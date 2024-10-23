package snownee.boattweaks.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import snownee.boattweaks.BoatSettings;
import snownee.boattweaks.duck.BTBoostingBoat;
import snownee.boattweaks.duck.BTConfigurableBoat;
import snownee.boattweaks.duck.BTMovementDistance;

@Mixin(value = Boat.class, priority = 900)
public abstract class BoatSettingsMixin extends VehicleEntity implements BTConfigurableBoat {

	@Shadow
	private Boat.Status status;
	@Shadow
	private boolean inputUp;
	@Shadow
	private boolean inputDown;
	@Shadow
	private boolean inputLeft;
	@Shadow
	private boolean inputRight;
	@Shadow
	private float deltaRotation;
	@Unique
	private int wallHitCd;
	@Unique
	@Nullable
	private BoatSettings settings;

	private BoatSettingsMixin(final EntityType<?> entityType, final Level level) {
		super(entityType, level);
	}

	@WrapOperation(
			method = "getGroundFriction",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F")
	)
	private float getGroundFriction(final Block block, final Operation<Float> original) {
		return boattweaks$getSettings().frictionOverrides().containsKey(block)
				? boattweaks$getSettings().frictionOverrides().getFloat(block)
				: original.call(block);
	}

	@ModifyVariable(method = "controlBoat", at = @At(value = "STORE", ordinal = 0), index = 1)
	private float modifyForce(float f) {
		BoatSettings settings = boattweaks$getSettings();
		float distance = ((BTMovementDistance) this).boattweaks$getDistance();
		if (status == Boat.Status.ON_LAND) {
			if (inputUp) {
				BTBoostingBoat boat = (BTBoostingBoat) this;
				f += settings.forwardForce() - 0.04F + boat.boattweaks$getExtraForwardForce();
			}
			if (inputDown) {
				f -= settings.backwardForce() - 0.005F;
			}
			f = settings.getDegradedForce(f, distance);
			if (inputLeft) {
				deltaRotation += 1 - settings.getDegradedForce(settings.turningForce(), distance);
			}
			if (inputRight) {
				deltaRotation -= 1 - settings.getDegradedForce(settings.turningForce(), distance);
			}
		} else if (status == Boat.Status.IN_AIR) {
			if (inputLeft) {
				deltaRotation += 1 - settings.getDegradedForce(settings.turningForceInAir(), distance);
			}
			if (inputRight) {
				deltaRotation -= 1 - settings.getDegradedForce(settings.turningForceInAir(), distance);
			}
		}
		return f;
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tick(CallbackInfo ci) {
		Boat boat = (Boat) (Object) this;
		if (wallHitCd > 0) {
			wallHitCd--;
		} else if (boat.horizontalCollision) {
			BoatSettings settings = boattweaks$getSettings();
			wallHitCd = settings.wallHitCooldown();
			float scale = 1 - settings.wallHitSpeedLoss();
			boat.setDeltaMovement(boat.getDeltaMovement().multiply(scale, 1, scale));
		}
	}

	@ModifyConstant(method = "tick", constant = @Constant(floatValue = 60F))
	private float modifyTimeOutTicks(float f) {
		return boattweaks$getSettings().outOfControlTicks();
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (settings != null) {
			compoundTag.put("BoatTweaksSettings", BoatSettings.CODEC.encodeStart(NbtOps.INSTANCE, settings).getOrThrow());
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		if (compoundTag.contains("BoatTweaksSettings")) {
			boattweaks$setSettings(BoatSettings.CODEC.parse(NbtOps.INSTANCE, compoundTag.getCompound("BoatTweaksSettings")).getOrThrow());
		}
	}

	@Override
	public BoatSettings boattweaks$getSettings() {
		if (settings == null) {
			return BoatSettings.DEFAULT;
		}
		return settings;
	}

	@Override
	public void boattweaks$setSettings(@Nullable BoatSettings settings) {
		this.settings = settings;
	}

	@Override
	public float maxUpStep() {
		return boattweaks$getSettings().stepUpHeight();
	}
}
