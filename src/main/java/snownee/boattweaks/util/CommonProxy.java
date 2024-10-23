package snownee.boattweaks.util;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import snownee.boattweaks.BoatSettings;
import snownee.boattweaks.BoatTweaks;
import snownee.boattweaks.BoatTweaksCommonConfig;
import snownee.boattweaks.duck.BTServerPlayer;
import snownee.boattweaks.network.SSyncSettingsPacket;
import snownee.boattweaks.network.SUpdateGhostModePacket;
import snownee.kiwi.Mod;
import snownee.kiwi.config.KiwiConfigManager;
import snownee.kiwi.network.KPacketSender;

@Mod(BoatTweaks.ID)
public class CommonProxy implements ModInitializer {
	private static String version;

	public static String getVersion() {
		return version;
	}

	@Override
	public void onInitialize() {
		version = FabricLoader.getInstance().getModContainer(BoatTweaks.ID).map(container -> container.getMetadata()
				.getVersion()
				.getFriendlyString()).orElseThrow();
		// Currently in 1.19.2, the serverInit method has a bug that it will not be called for integrated server.
		ServerLifecycleEvents.SERVER_STARTING.register($ -> {
			if (!$.isDedicatedServer()) {
				KiwiConfigManager.getHandler(BoatTweaksCommonConfig.class).refresh();
			}
			BoatTweaksCommonConfig.refresh();
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			KPacketSender.send(new SSyncSettingsPacket(BoatSettings.DEFAULT, Integer.MIN_VALUE), handler.player);
			if (handler.player.level().getGameRules().getBoolean(BoatTweaks.GHOST_MODE)) {
				KPacketSender.send(new SUpdateGhostModePacket(true), handler.player);
			}
		});
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
			boolean verified = ((BTServerPlayer) oldPlayer).boattweaks$isVerified();
			((BTServerPlayer) newPlayer).boattweaks$setVerified(verified);
		});
	}
}
