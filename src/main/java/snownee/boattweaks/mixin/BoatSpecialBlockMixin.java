package snownee.boattweaks.mixin;

import java.util.IdentityHashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMaps;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.boattweaks.BoatSettings;
import snownee.boattweaks.BoatTweaks;
import snownee.boattweaks.duck.BTBoostingBoat;
import snownee.boattweaks.duck.BTConfigurableBoat;

@Mixin(Boat.class)
public abstract class BoatSpecialBlockMixin implements BTBoostingBoat {

	@Final
	@Unique
	private Reference2IntMap<Block> specialBlockCooldowns;
	@Unique
	private final Map<Block, BlockPos> specialBlockRecords = new IdentityHashMap<>(Math.min(BoatTweaks.CUSTOM_SPECIAL_BLOCKS.size(), 10));
	@Unique
	private int eject;
	@Unique
	private int boostTicks;

	@Inject(method = "<init>*", at = @At("RETURN"))
	private void init(final CallbackInfo ci) {
		//noinspection ShadowFinalModification
		specialBlockCooldowns = new Reference2IntOpenHashMap<>(Math.min(BoatTweaks.CUSTOM_SPECIAL_BLOCKS.size(), 10));
	}

	@Inject(
			method = "getGroundFriction",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getFriction()F")
	)
	private void getGroundFriction(
			final CallbackInfoReturnable<Float> cir,
			@Local BlockPos.MutableBlockPos pos,
			@Local BlockState blockState) {
		Boat boat = (Boat) (Object) this;
		BoatSettings settings = ((BTConfigurableBoat) boat).boattweaks$getSettings();
		if (boat.isControlledByLocalInstance() && blockState.is(settings.ejectingBlock())) {
			if (this.eject == 0 && boat.level().isClientSide && !boat.isSilent()) {
				boat.level().playLocalSound(
						boat.getX(),
						boat.getY(),
						boat.getZ(),
						BoatTweaks.EJECT.get(),
						boat.getSoundSource(),
						1.0F,
						1.0F,
						false);
			}
			int eject = 1;
			int y = pos.getY();
			BlockState blockState1;
			while (true) {
				pos.setY(y - eject);
				blockState1 = boat.level().getBlockState(pos);
				if (!blockState1.is(settings.ejectingBlock())) {
					break;
				}
				eject++;
			}
			this.eject = Math.max(this.eject, eject);
			pos.setY(y);
		}
		if (boostTicks < settings.boostingTicks() && blockState.is(settings.boostingBlock())) {
			if (settings.boostingTicks() - boostTicks > 10) {
				boat.playSound(BoatTweaks.BOOST.get());
			}
			boostTicks = settings.boostingTicks();
		}
		Block block = blockState.getBlock();
		int cooldown = BoatTweaks.CUSTOM_SPECIAL_BLOCKS.getInt(block);
		if (cooldown > 0 && specialBlockCooldowns.getInt(block) == 0) {
			specialBlockCooldowns.put(block, cooldown);
			specialBlockRecords.put(block, pos.immutable());
		}
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci) {
		Boat boat = (Boat) (Object) this;
		BoatSettings settings = ((BTConfigurableBoat) boat).boattweaks$getSettings();
		if (eject > 0) {
			float force;
			if (eject == 1) {
				force = settings.ejectingForce();
			} else {
				force = settings.ejectingForce() * (float) (Math.pow(1.1, eject - 1));
			}
			eject = 0;
			boat.setDeltaMovement(boat.getDeltaMovement().with(Direction.Axis.Y, force));
		}
		if (boostTicks > 0) {
			boostTicks--;
		}

		Reference2IntMaps.fastIterator(specialBlockCooldowns).forEachRemaining(entry -> {
			Block block = entry.getKey();
			int cooldown = entry.getIntValue();
			if (BoatTweaks.CUSTOM_SPECIAL_BLOCKS.getInt(block) == cooldown) {
				BlockPos pos = specialBlockRecords.get(block);
				if (pos != null) {
					BlockState blockState = boat.level().getBlockState(pos);
					if (blockState.is(block)) {
						BoatTweaks.postSpecialBlockEvent(boat, blockState, pos);
					}
				}
			}
			if (cooldown > 0) {
				entry.setValue(cooldown - 1);
			}
		});
	}

	@Override
	public float boattweaks$getExtraForwardForce() {
		return boostTicks > 0 ? ((BTConfigurableBoat) this).boattweaks$getSettings().boostingForce() : 0;
	}
}
