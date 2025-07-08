package lpctools.tools.antiSpawner;

import lpctools.compact.derived.ShapeList;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import static lpctools.generic.GenericUtils.mayMobSpawnAt;
import static lpctools.lpcfymasaapi.configbutton.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.antiSpawner.AntiSpawner.*;
import static lpctools.tools.antiSpawner.AntiSpawnerData.*;
import static lpctools.util.DataUtils.notifyPlayer;

public class AntiSpawnerRunner implements ClientTickEvents.EndTick {
    @Override public void onEndTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            ASConfig.setBooleanValue(false);
            return;
        }
        if (mc.currentScreen != null) return;
        if (HandRestock.search(restockTest, 0) == -1) return;
        ShapeList shapeList = rangeLimitConfig.buildShapeList();
        limitOperationSpeedConfig.resetOperationTimes();
        //默认遍历的距离判断是与方块中心的距离，但是这里选择interact底下方块的上表面中心，所以添加了一个y+0.5的偏移修正
        limitOperationSpeedConfig.iterableOperate(
            reachDistanceConfig.iterateFromClosest(mc.player.getEyePos().add(0, 0.5, 0)),
            pos -> {
                if (!shapeList.testPos(pos)) return NO_OPERATION;
                if (!mayMobSpawnAt(mc.world, mc.world.getLightingProvider(), pos)) return NO_OPERATION;
                if (!mc.world.getBlockState(pos).isReplaceable()) return NO_OPERATION;
                BlockPos downPos = pos.down();
                BlockPos hitPos;
                if (mc.world.getBlockState(pos.down()).isReplaceable()) hitPos = pos;
                else hitPos = downPos;
                BlockHitResult hitResult = new BlockHitResult(
                    pos.toBottomCenterPos(), Direction.UP, hitPos, false);
                if (!mc.player.isSneaking()) {
                    BlockState below = mc.world.getBlockState(pos.down());
                    ActionResult result = below.onUse(mc.world, mc.player, hitResult);
                    if (result == ActionResult.SUCCESS) {
                        notifyPlayer(String.format("onUse at %s", pos.down().toString()), false);
                        return NO_OPERATION;
                    }
                }
                limitOperationSpeedConfig.limitWithRestock(restockTest, 0);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                return OPERATED;
            });
    }
}
