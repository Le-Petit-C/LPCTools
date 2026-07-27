package lpctools.tools.spawnProofer;

import lpctools.compact.derived.ShapeList;
import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.tools.ToolUtils;
import lpctools.util.HandRestock;
import lpctools.util.inGame.InGameManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import static lpctools.tools.spawnProofer.SpawnProofer.*;

public class SpawnProoferRunner implements ClientTickEvents.EndTick, ToolUtils.ToolRunner {
    @Override public void onEndTick(@NonNull Minecraft mc) {
        InGameManager manager = InGameManager.get(mc);
        if (manager == null) {
            ASConfig.setBooleanValue(false);
            return;
        }
        if (mc.gui.screen() != null) return;
        HandRestock.IRestockTest restockTest = item -> item.getItem() instanceof BlockItem blockItem && placeableItems.contains(blockItem);
        var limit = OperationSpeedLimit.root().limitWithRestock(restockTest, interactionHand.offhandPriority());
        if (HandRestock.search(restockTest, 0) == -1) return;
        ShapeList shapeList = rangeLimitConfig.buildShapeList();
        //默认遍历的距离判断是与方块中心的距离，但是这里选择interact底下方块的上表面中心，所以添加了一个y+0.5的偏移修正
        for(BlockPos pos : reachDistanceConfig.iterateFromClosest(manager.player.getEyePosition().add(0, 0.5, 0))) {
            if (!limit.hasReservedTimes()) break;
            if (!shapeList.testPos(pos)) continue;
            if (!manager.mayMobSpawnAt(pos)) continue;
            if (!manager.getBlockState(pos).canBeReplaced()) continue;
            BlockPos belowPos = pos.below();
            BlockPos hitPos;
            if (manager.getBlockState(belowPos).canBeReplaced()) hitPos = pos.immutable();
            else hitPos = belowPos;
            BlockHitResult hitResult = new BlockHitResult(
                Vec3.atBottomCenterOf(pos), Direction.UP, hitPos, false);
            // 非潜行时试探下方方块是否会拦截右键交互（如拉杆、按钮、工作台、熔炉等），若是则跳过此位置
            if (!manager.isShiftKeyDown()) {
                BlockState belowState = manager.getBlockState(belowPos);
                InteractionResult result = belowState.useWithoutItem(manager.level, manager.player, hitResult);
                if (result == InteractionResult.SUCCESS) continue;
            }
            manager.useItemOn(interactionHand.getHand(), hitResult);
            limit.costInteractBlock();
        }
    }

    @Override public void registerAll(boolean b) { Registries.END_CLIENT_TICK.register(this, b); }
}
