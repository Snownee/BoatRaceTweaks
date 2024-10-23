package snownee.boattweaks.network;

import java.util.Objects;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.boattweaks.BoatTweaks;
import snownee.boattweaks.duck.BTServerPlayer;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public class CPingServerPacket implements CustomPacketPayload {
	public static final Type<CPingServerPacket> TYPE = new CustomPacketPayload.Type<>(BoatTweaks.RL("ping"));
	public static final CPingServerPacket INSTANCE = new CPingServerPacket();

	public static void ping() {
		KPacketSender.sendToServer(INSTANCE);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final class Handler implements PlayPacketHandler<CPingServerPacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, CPingServerPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public void handle(CPingServerPacket packet, PayloadContext context) {
			context.execute(() -> {
				var player = context.serverPlayer();
				BoatTweaks.LOGGER.info("Received config from {}", Objects.requireNonNull(player).getGameProfile().getName());
				((BTServerPlayer) player).boattweaks$setVerified(true);
			});
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, CPingServerPacket> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
