package snownee.boattweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import snownee.boattweaks.duck.BTMovementDistance;
import snownee.boattweaks.network.SSyncDistancePacket;
import snownee.kiwi.network.KPacketSender;

@Mixin(value = Boat.class, priority = 1500)
public class BoatMovementDistanceMixin implements BTMovementDistance {

	@Shadow
	private Boat.Status status;
	@Shadow
	private Boat.Status oldStatus;
	@Unique
	private float lastX;
	@Unique
	private float lastZ;
	@Unique
	private float distance;
	@Unique
	private float unsyncedDistance;

	@Override
	public float boattweaks$getDistance() {
		return distance;
	}

	@Override
	public void boattweaks$setDistance(float distance) {
		this.distance = distance;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void postTick(CallbackInfo ci) {
		Boat boat = (Boat) (Object) this;
		if (boat.level().isClientSide) {
			return;
		}
		updateDistance();
		lastX = (float) boat.getX();
		lastZ = (float) boat.getZ();
		if (unsyncedDistance >= 5) {
			if (boat.getControllingPassenger() instanceof ServerPlayer player) {
				KPacketSender.send(new SSyncDistancePacket(boat.getId(), boattweaks$getDistance()), player);
			}
			unsyncedDistance = 0;
		}
	}

	@Unique
	private void updateDistance() {
		Boat boat = (Boat) (Object) this;
		if (boat.tickCount == 1) {
			return;
		}
		if (oldStatus != Boat.Status.ON_LAND && oldStatus != Boat.Status.IN_AIR) {
			return;
		}
		if (status != Boat.Status.ON_LAND && status != Boat.Status.IN_AIR) {
			return;
		}
		float dx = Mth.abs((float) (lastX - boat.getX()));
		float dz = Mth.abs((float) (lastZ - boat.getZ()));
		unsyncedDistance += dx + dz;
		if (dx < 0.001 && dz < 0.001) {
			return;
		}
		boattweaks$setDistance(boattweaks$getDistance() + Mth.sqrt(dx * dx + dz * dz));
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void addAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		compoundTag.putFloat("BoatTweaksDistance", boattweaks$getDistance());
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		boattweaks$setDistance(compoundTag.getFloat("BoatTweaksDistance"));
	}
}
