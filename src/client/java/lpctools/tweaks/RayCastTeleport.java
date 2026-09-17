package lpctools.tweaks;

import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.HotkeyConfig;
import lpctools.util.inGame.InGameManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.*;

public class RayCastTeleport {
	public static HotkeyConfig rayCastTeleport = new HotkeyConfig(TweakConfigs.tweaks, "rayCastTeleport", null, RayCastTeleport::teleport);
	public static DoubleConfig maxTeleportDistance = new DoubleConfig(TweakConfigs.tweaks, "maxTeleportDistance", 64, 0, 65536);
	public static BooleanConfig cancelTeleportWhenMiss = new BooleanConfig(TweakConfigs.tweaks, "cancelTeleportWhenMiss", false);
	private static boolean teleport(KeyAction action, IKeybind keybind) {
		Minecraft mc = Minecraft.getInstance();
		InGameManager manager = InGameManager.get(mc);
		if(manager == null) return false;
		if(!GenericUtils.hasCommand(manager, "tp", true)) return false;
		HitResult hitResult = manager.player.pick(Math.min(maxTeleportDistance.getAsDouble(), loadedEdgeDistance(manager)), 1.0f, false);
		if(cancelTeleportWhenMiss.getBooleanValue() && hitResult.getType() == HitResult.Type.MISS) return true;
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

	/**
	 * 沿视线求出“已加载区域边缘”的距离，作为射线长度的上限（内部行为，无配置）。<br>
	 * 看着虚空或天顶时最多延伸到渲染距离的两倍，也还算合理且似乎比较保守的？<br>
	 * 感觉优化有点麻烦，也没必要做很高效的，就没进一步优化了。
	 */
	private static double loadedEdgeDistance(InGameManager manager) {
		Vec3 from = manager.player.getEyePosition(1.0f);
		Vec3 view = manager.player.getViewVector(1.0f);
		double d = Minecraft.getInstance().options.renderDistance().get() * 16;
		double distance = 0;
		while (d > 1e-7) {
			double nextDistance = distance + d;
			BlockPos pos = BlockPos.containing(from.add(view.scale(nextDistance)));
			if(manager.isLoadedIgnoreHeight(pos)) distance = nextDistance;
			d *= 0.5;
		}
		return distance;
	}
}
