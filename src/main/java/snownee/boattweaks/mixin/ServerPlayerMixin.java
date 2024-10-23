package snownee.boattweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import snownee.boattweaks.duck.BTServerPlayer;
import snownee.boattweaks.network.SSyncDistancePacket;
import snownee.kiwi.network.KPacketSender;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements BTServerPlayer {
	@Unique
	private boolean verified;

	@Inject(method = "startRiding", at = @At("HEAD"))
	private void startRiding(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> ci) {
		if (!(entity instanceof Boat boat)) {
			return;
		}
		KPacketSender.send(new SSyncDistancePacket(boat), (ServerPlayer) (Object) this);
	}

	@Override
	public void boattweaks$setVerified(boolean value) {
		this.verified = value;
	}

	@Override
	public boolean boattweaks$isVerified() {
		return verified;
	}
}
