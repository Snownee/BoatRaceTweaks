package snownee.boattweaks.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import snownee.boattweaks.BoatTweaksUtil;
import snownee.boattweaks.network.SSyncSettingsPacket;
import snownee.kiwi.network.KPacketSender;

@Mixin(Entity.class)
public class BoatSettingsEntityMixin {
	@Inject(method = "startSeenByPlayer", at = @At("HEAD"))
	private void startSeenByPlayer(ServerPlayer player, CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;
		if (entity instanceof Boat boat && !BoatTweaksUtil.isDefaultSettings(boat)) {
			KPacketSender.send(new SSyncSettingsPacket(BoatTweaksUtil.getBoatSettings(boat), boat.getId()), player);
		}
	}
}
