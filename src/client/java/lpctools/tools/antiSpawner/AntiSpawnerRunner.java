package lpctools.tools.antiSpawner;

import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericUtils;
import lpctools.util.DataUtils;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import static lpctools.lpcfymasaapi.configButtons.derivedConfigs.LimitOperationSpeedConfig.OperationResult.*;
import static lpctools.tools.antiSpawner.AntiSpawner.*;
import static lpctools.tools.antiSpawner.AntiSpawnerData.*;

public class AntiSpawnerRunner implements ClientTickEvents.EndTick {
    @Override public void onEndTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            ASConfig.setBooleanValue(false);
            return;
        }
        if (mc.screen != null) return;
        if (HandRestock.search(restockTest, 0) == -1) return;
        ShapeList shapeList = rangeLimitConfig.buildShapeList();
        limitOperationSpeedConfig.resetOperationTimes();
        //默认遍历的距离判断是与方块中心的距离，但是这里选择interact底下方块的上表面中心，所以添加了一个y+0.5的偏移修正
        GenericUtils.MobSpawnTest spawnTest = GenericUtils.createSpawnTest();
        limitOperationSpeedConfig.iterableOperate(
            reachDistanceConfig.iterateFromClosest(mc.player.getEyePosition().add(0, 0.5, 0)),
            pos -> {
                if (!shapeList.testPos(pos)) return NO_OPERATION;
                if (!spawnTest.mayMobSpawnAt(mc.level, mc.level.getLightEngine(), pos)) return NO_OPERATION;
                if (!mc.level.getBlockState(pos).canBeReplaced()) return NO_OPERATION;
                BlockPos downPos = pos.below();
                BlockPos hitPos;
                if (mc.level.getBlockState(pos.below()).canBeReplaced()) hitPos = pos;
                else hitPos = downPos;
                BlockHitResult hitResult = new BlockHitResult(
                    pos.getBottomCenter(), Direction.UP, hitPos, false);
                if (!mc.player.isShiftKeyDown()) {
                    BlockState below = mc.level.getBlockState(pos.below());
                    InteractionResult result = below.useWithoutItem(mc.level, mc.player, hitResult);
                    if (result == InteractionResult.SUCCESS) {
                        DataUtils.clientMessage(String.format("onUse at %s", pos.below().toString()), false);
                        return NO_OPERATION;
                    }
                }
                limitOperationSpeedConfig.limitWithRestock(restockTest, 0);
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                return OPERATED;
            });
    }
}
