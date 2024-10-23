package snownee.boattweaks;

import java.util.List;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.boattweaks.network.SUpdateGhostModePacket;
import snownee.kiwi.AbstractModule;
import snownee.kiwi.KiwiGO;
import snownee.kiwi.KiwiModule;
import snownee.kiwi.network.KPacketSender;
import snownee.kiwi.util.KUtil;

@KiwiModule
public class BoatTweaks extends AbstractModule {

	public static final String ID = "boattweaks";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final KiwiGO<SoundEvent> BOOST = go(() -> SoundEvent.createVariableRangeEvent(RL("boost")));
	public static final KiwiGO<SoundEvent> EJECT = go(() -> SoundEvent.createVariableRangeEvent(RL("eject")));
	public static final GameRules.Key<GameRules.BooleanValue> AUTO_REMOVE_BOAT = GameRuleRegistry.register(
			ID + ":autoRemoveBoat",
			GameRules.Category.MISC,
			GameRuleFactory.createBooleanRule(false)
	);
	public static final GameRules.Key<GameRules.BooleanValue> GHOST_MODE = GameRuleRegistry.register(
			ID + ":ghostMode",
			GameRules.Category.MISC,
			GameRuleFactory.createBooleanRule(false, (server, rule) -> {
				server.getPlayerList().getPlayers().forEach(p -> {
					KPacketSender.send(new SUpdateGhostModePacket(rule.get()), p);
				});
			})
	);
	public static final Reference2IntMap<Block> CUSTOM_SPECIAL_BLOCKS = new Reference2IntOpenHashMap<>(8);
	public static final List<SpecialBlockEvent> SPECIAL_BLOCK_LISTENERS = Lists.newArrayList();

	public static void postSpecialBlockEvent(Boat boat, BlockState blockState, BlockPos blockPos) {
		SPECIAL_BLOCK_LISTENERS.forEach(listener -> listener.on(boat, blockState, blockPos));
	}

	public static ResourceLocation RL(String path) {
		return KUtil.RL(path, ID);
	}
}
