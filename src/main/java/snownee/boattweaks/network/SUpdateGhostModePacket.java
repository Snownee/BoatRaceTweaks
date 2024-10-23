package snownee.boattweaks.network;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import snownee.boattweaks.BoatTweaks;
import snownee.boattweaks.duck.BTClientPacketListener;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SUpdateGhostModePacket(
		boolean value
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SUpdateGhostModePacket> TYPE = new CustomPacketPayload.Type<>(BoatTweaks.RL(
			"update_ghost_mode"));
	public static SUpdateGhostModePacket I;

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final class Handler implements PlayPacketHandler<SUpdateGhostModePacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SUpdateGhostModePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL,
				SUpdateGhostModePacket::value,
				SUpdateGhostModePacket::new
		);

		@Override
		public void handle(SUpdateGhostModePacket packet, PayloadContext context) {
			context.execute(() -> (
					(BTClientPacketListener) Objects.requireNonNull(Minecraft.getInstance()
							.getConnection())).boattweaks$setGhostMode(packet.value()));
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SUpdateGhostModePacket> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
