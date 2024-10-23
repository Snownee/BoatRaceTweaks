package snownee.boattweaks;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.Level;
import snownee.boattweaks.duck.BTClientPacketListener;
import snownee.boattweaks.duck.BTConfigurableBoat;
import snownee.boattweaks.duck.BTMovementDistance;

public class BoatTweaksUtil {
	public static boolean isClientSide(Level level, String method) {
		if (level.isClientSide) {
			BoatTweaks.LOGGER.warn("BoatTweaksUtil#{} can only be called on server side.", method);
		}
		return level.isClientSide;
	}

	public static float getMovementDistance(Boat boat) {
		return ((BTMovementDistance) boat).boattweaks$getDistance();
	}

	public static void setMovementDistance(Boat boat, float distance) {
		if (isClientSide(boat.level(), "setMovementDistance")) {
			return;
		}
		((BTMovementDistance) boat).boattweaks$setDistance(distance);
	}

	public static BoatSettings getBoatSettings(Boat boat) {
		return ((BTConfigurableBoat) boat).boattweaks$getSettings();
	}

	public static void setBoatSettings(Boat boat, @Nullable BoatSettings settings) {
		if (isClientSide(boat.level(), "setBoatSettings")) {
			return;
		}
		((BTConfigurableBoat) boat).boattweaks$setSettings(settings);
	}

	public static boolean isGhostMode(Level level) {
		if (level.isClientSide) {
			return ((BTClientPacketListener) Objects.requireNonNull(Minecraft.getInstance().getConnection())).boattweaks$getGhostMode();
		} else {
			return level.getGameRules().getBoolean(BoatTweaks.GHOST_MODE);
		}
	}

	public static boolean isDefaultSettings(Boat boat) {
		return getBoatSettings(boat) == BoatSettings.DEFAULT;
	}
}
