package snownee.boattweaks.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.vehicle.Boat;
import snownee.boattweaks.BoatTweaks;
import snownee.boattweaks.duck.BTMovementDistance;
import snownee.kiwi.network.KiwiPacket;
import snownee.kiwi.network.PayloadContext;
import snownee.kiwi.network.PlayPacketHandler;

@KiwiPacket
public record SSyncDistancePacket(
		int entityId,
		float distance
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SSyncDistancePacket> TYPE = new CustomPacketPayload.Type<>(BoatTweaks.RL("sync_distance"));

	public SSyncDistancePacket(Boat boat) {
		this(boat.getId(), ((BTMovementDistance) boat).boattweaks$getDistance());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static final class Handler implements PlayPacketHandler<SSyncDistancePacket> {
		public static final StreamCodec<RegistryFriendlyByteBuf, SSyncDistancePacket> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT,
				SSyncDistancePacket::entityId,
				ByteBufCodecs.FLOAT,
				SSyncDistancePacket::distance,
				SSyncDistancePacket::new
		);

		@Override
		public void handle(SSyncDistancePacket packet, PayloadContext context) {
			context.execute(() -> {
				if (Minecraft.getInstance().level.getEntity(packet.entityId()) instanceof Boat boat) {
					((BTMovementDistance) boat).boattweaks$setDistance(packet.distance());
				}
			});
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, SSyncDistancePacket> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
