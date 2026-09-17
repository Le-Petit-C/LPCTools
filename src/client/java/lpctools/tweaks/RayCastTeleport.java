package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.util.inGame.InGameManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.*;

public class RayCastTeleport {
	public static HotkeyConfig rayCastTeleport = new HotkeyConfig(TweakConfigs.tweaks, "rayCastTeleport", null, RayCastTeleport::teleport);
	public static DoubleConfig maxTeleportDistance = new DoubleConfig(TweakConfigs.tweaks, "maxTeleportDistance", 1024, 0, 65536);
	private static boolean teleport(KeyAction action, IKeybind keybind) {
		Minecraft mc = Minecraft.getInstance();
		InGameManager manager = InGameManager.get(mc);
		if(manager == null) return false;
		if(!GenericUtils.hasCommand(manager, "tp", true)) return false;
		HitResult hitResult = manager.player.pick(maxTeleportDistance.getAsDouble(), 1.0f, false);
		if(hitResult.getType() == HitResult.Type.MISS) return true;
		Vec3 hitLocation = hitResult.getLocation();
		Vec3 location;
		switch (hitResult) {
			case EntityHitResult _ -> location = hitLocation;
			case BlockHitResult blockHit -> {
				Direction hitDirection = blockHit.getDirection();
				switch (hitDirection) {
					case DOWN -> {
						Vec3 tempLocation = hitLocation.relative(Direction.DOWN, manager.playerHeight());
						if(manager.noBlockCollision(manager.playerDimensions().makeBoundingBox(tempLocation)))
							location = tempLocation;
						else {
							EntityDimensions crouching = manager.playerDimensions(Pose.CROUCHING);
							Vec3 tempLocation2 = hitLocation.relative(Direction.DOWN, crouching.height());
							if(manager.noBlockCollision(crouching.makeBoundingBox(tempLocation2)))
								location = tempLocation2;
							else location = hitLocation.relative(Direction.DOWN, manager.playerDimensions(Pose.SWIMMING).height());
						}
					}
					case UP -> location = hitLocation;
					default -> location = hitLocation.relative(hitDirection, manager.playerWidth() / 2);
				}
			}
			default -> { return true; }
		}
		manager.sendCommand(String.format("tp %f %f %f", location.x, location.y, location.z));
		return true;
	}
}
