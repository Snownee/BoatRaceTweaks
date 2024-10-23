package snownee.boattweaks.network;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.vehicle.Boat;
import snownee.boattweaks.BoatSettings;
import snownee.boattweaks.BoatTweaks;
import snownee.boattweaks.duck.BTConfigurableBoat;
import snownee.boattweaks.util.CommonProxy;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SSyncSettingsPacket(
		String version,
		BoatSettings settings,
		int entityId
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SSyncSettingsPacket> TYPE = new CustomPacketPayload.Type<>(BoatTweaks.RL("sync_settings"));

	public SSyncSettingsPacket(BoatSettings settings, int entityId) {
		this(CommonProxy.getVersion(), settings, entityId);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final class Handler implements PlayPacketHandler<SSyncSettingsPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SSyncSettingsPacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.STRING_UTF8,
				SSyncSettingsPacket::version,
				BoatSettings.STREAM_CODEC,
				SSyncSettingsPacket::settings,
				ByteBufCodecs.INT,
				SSyncSettingsPacket::entityId,
				SSyncSettingsPacket::new
		);

		@Override
		public void handle(SSyncSettingsPacket packet, PayloadContext context) {
			if (!Objects.equals(packet.version(), CommonProxy.getVersion())) {
				return;
			}
			context.execute(() -> {
				if (packet.entityId() == Integer.MIN_VALUE) {
					BoatSettings.DEFAULT = packet.settings();
					KPacketSender.sendToServer(CPingServerPacket.INSTANCE);
				} else if (Objects.requireNonNull(Minecraft.getInstance().level).getEntity(packet.entityId()) instanceof Boat boat) {
					((BTConfigurableBoat) boat).boattweaks$setSettings(packet.settings());
				}
			});
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SSyncSettingsPacket> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
