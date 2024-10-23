package snownee.boattweaks;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Reference2FloatMap;
import it.unimi.dsi.fastutil.objects.Reference2FloatMaps;
import it.unimi.dsi.fastutil.objects.Reference2FloatOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

// Normalize it to the vanilla values
public record BoatSettings(
		Reference2FloatMap<Block> frictionOverrides,
		float forwardForce,
		float backwardForce,
		float turningForce,
		float turningForceInAir,
		float stepUpHeight,
		float outOfControlTicks,
		Block boostingBlock,
		int boostingTicks,
		float boostingForce,
		Block ejectingBlock,
		float ejectingForce,
		float wallHitSpeedLoss,
		int wallHitCooldown,
		float degradeForceLossPerMeter,
		int degradeForceLossStartFrom,
		float degradeForceMaxLoss) {
	public static BoatSettings DEFAULT = new BoatSettings();

	public static final Codec<BoatSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					Codec.simpleMap(
							BuiltInRegistries.BLOCK.byNameCodec(),
							Codec.FLOAT,
							BuiltInRegistries.BLOCK).<Reference2FloatMap<Block>>xmap(
							Reference2FloatOpenHashMap::new,
							Reference2FloatMaps::unmodifiable).fieldOf("frictionOverrides").forGetter(BoatSettings::frictionOverrides),
					Codec.FLOAT.fieldOf("forwardForce").forGetter(BoatSettings::forwardForce),
					Codec.FLOAT.fieldOf("backwardForce").forGetter(BoatSettings::backwardForce),
					Codec.FLOAT.fieldOf("turningForce").forGetter(BoatSettings::turningForce),
					Codec.FLOAT.fieldOf("turningForceInAir").forGetter(BoatSettings::turningForceInAir),
					Codec.FLOAT.fieldOf("stepUpHeight").forGetter(BoatSettings::stepUpHeight),
					Codec.FLOAT.fieldOf("outOfControlTicks").forGetter(BoatSettings::outOfControlTicks),
					BuiltInRegistries.BLOCK.byNameCodec().fieldOf("boostingBlock").forGetter(BoatSettings::boostingBlock),
					Codec.INT.fieldOf("boostingTicks").forGetter(BoatSettings::boostingTicks),
					Codec.FLOAT.fieldOf("boostingForce").forGetter(BoatSettings::boostingForce),
					BuiltInRegistries.BLOCK.byNameCodec().fieldOf("ejectingBlock").forGetter(BoatSettings::ejectingBlock),
					Codec.FLOAT.fieldOf("ejectingForce").forGetter(BoatSettings::ejectingForce),
					Codec.FLOAT.fieldOf("wallHitSpeedLoss").forGetter(BoatSettings::wallHitSpeedLoss),
					Codec.INT.fieldOf("wallHitCooldown").forGetter(BoatSettings::wallHitCooldown),
					Codec.FLOAT.fieldOf("degradeForceLossPerMeter").forGetter(BoatSettings::degradeForceLossPerMeter),
					Codec.mapPair(Codec.INT.fieldOf("degradeForceLossStartFrom"), Codec.FLOAT.fieldOf("degradeForceMaxLoss"))
							.forGetter(it -> Pair.of(it.degradeForceLossStartFrom(), it.degradeForceMaxLoss())))
			.apply(instance, BoatSettings::new));
	public static final StreamCodec<ByteBuf, BoatSettings> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

	public BoatSettings(
			Reference2FloatMap<Block> frictionOverrides,
			float forwardForce,
			float backwardForce,
			float turningForce,
			float turningForceInAir,
			float stepUpHeight,
			float outOfControlTicks,
			Block boostingBlock,
			int boostingTicks,
			float boostingForce,
			Block ejectingBlock,
			float ejectingForce,
			float wallHitSpeedLoss,
			int wallHitCooldown,
			float degradeForceLossPerMeter,
			Pair<Integer, Float> pair0) {
		this(
				frictionOverrides,
				forwardForce,
				backwardForce,
				turningForce,
				turningForceInAir,
				stepUpHeight,
				outOfControlTicks,
				boostingBlock,
				boostingTicks,
				boostingForce,
				ejectingBlock,
				ejectingForce,
				wallHitSpeedLoss,
				wallHitCooldown,
				degradeForceLossPerMeter,
				pair0.getFirst(),
				pair0.getSecond());
	}

	public static BoatSettings fromLocal() {
		return new BoatSettings(
				BoatTweaksCommonConfig.frictionOverrides.entrySet()
						.stream()
						.reduce(new Reference2FloatOpenHashMap<>(BoatTweaksCommonConfig.frictionOverrides.size()), (acc, entry) -> {
							var block = BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(entry.getKey()));
							if (block != Blocks.AIR) {
								acc.put(block, entry.getValue().floatValue());
							}
							return acc;
						}, (first, second) -> second),
				BoatTweaksCommonConfig.forwardForce,
				BoatTweaksCommonConfig.backwardForce,
				BoatTweaksCommonConfig.turningForce,
				BoatTweaksCommonConfig.turningForceInAir,
				BoatTweaksCommonConfig.stepUpHeight,
				BoatTweaksCommonConfig.outOfControlTicks,
				BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(BoatTweaksCommonConfig.boostingBlock)),
				BoatTweaksCommonConfig.boostingTicks,
				BoatTweaksCommonConfig.boostingForce,
				BuiltInRegistries.BLOCK.get(ResourceLocation.tryParse(BoatTweaksCommonConfig.ejectingBlock)),
				BoatTweaksCommonConfig.ejectingForce,
				BoatTweaksCommonConfig.wallHitSpeedLoss,
				BoatTweaksCommonConfig.wallHitCooldown,
				BoatTweaksCommonConfig.degradeForceLossPerMeter,
				BoatTweaksCommonConfig.degradeForceLossStartFrom,
				BoatTweaksCommonConfig.degradeForceMaxLoss);
	}

	public BoatSettings(
	) {
		this(

				Reference2FloatMaps.emptyMap(), 0.04F, 0.005F, 1F, 1F, 0F, 60F, Blocks.AIR, 0, 0F, Blocks.AIR, 0F, 0F, 0, 0F, 0, 0F);
	}


	public float getDegradedForce(float force, float distance) {
		if (degradeForceMaxLoss == 0 || degradeForceLossPerMeter == 0 || distance <= degradeForceLossStartFrom) {
			return force;
		}
		return force * (1 - Math.min((distance - degradeForceLossStartFrom) * degradeForceLossPerMeter, degradeForceMaxLoss));
	}
}
