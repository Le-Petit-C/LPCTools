package lpctools.tools.fillingAssistant;

import lpctools.compact.derived.ShapeList;
import lpctools.generic.OperationSpeedLimit;
import lpctools.lpcfymasaapi.Registries;
import lpctools.tools.ToolUtils;
import lpctools.util.GuiUtils;
import lpctools.util.HandRestock;
import lpctools.util.inGame.InGameManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static lpctools.tools.fillingAssistant.FillingAssistant.*;
import static lpctools.util.BlockUtils.*;

public class FillingAssistantRunner implements ClientTickEvents.EndTick, Registries.InGameEndMouse, ToolUtils.ToolRunner {
    public void setTestDistance(int distance){
        if(testDistance == distance) return;
        testDistance = distance;
        testSize = distance * 2 + 1;
        map = new boolean[testSize][testSize][testSize];
        testBuffer = new boolean[testSize][testSize][testSize];
    }

    @Override public void onEndTick(@NonNull Minecraft client){
        if(client.isPaused()) return;
        InGameManager manager = InGameManager.get(client);
        if(manager == null) {
            disableTool("notInGame");
            return;
        }
        if (manager.gameType() == GameType.SPECTATOR || manager.gameType() == GameType.ADVENTURE){
            disableTool("unsupportedGameMode");
            return;
        }
        if(disableOnGUIOpened.getAsBoolean() && GuiUtils.isInTextOrGui()){
            disableTool("GUIOpened");
            return;
        }
        HandRestock.IRestockTest restockTest = new HandRestock.SearchInSet(getPlaceableItems());
        if(HandRestock.search(restockTest, interactionHand.offhandPriority()) == -1){//这个或许应该放在函数末尾，但是放在这里似乎也没什么坏处
            disableTool("placeableItemRanOut");
            return;
        }
        setTestDistance(testDistanceConfig.getAsInt());
        Vec3 eyePos = manager.playerEyePos();
        BlockPos eyeBlockPos = new BlockPos((int)Math.floor(eyePos.x()), (int)Math.floor(eyePos.y()), (int)Math.floor(eyePos.z()));
        ShapeList shapeList = limitFillingRange.buildShapeList();
        initializeMap(shapeList, eyeBlockPos, manager.level);
        DimensionType dimensionType = manager.dimensionType();
        int bottom = dimensionType.minY();
        int ceiling = bottom + dimensionType.height();
        var limit = OperationSpeedLimit.root().limitWithRestock(restockTest, interactionHand.offhandPriority());
        for(BlockPos pos : reachDistanceConfig.iterateFromFurthest(eyePos)) {
            if(!limit.hasReservedTimes()) break;
            if(pos.getY() < bottom) continue;
            if(pos.getY() >= ceiling) continue;
            if(!shapeList.testPos(pos)) continue;
            if(tryPut(manager, pos, limit)) {
                if(isUnpassable(pos)){
                    setMapVec3i(pos.subtract(currentPosition), true);
                    continue;
                }
            }
            if(!FAConfig.getBooleanValue()) break;
		}
    }
    @Override public void onInGameEndMouse(MouseButtonInfo input, int action) {
        if(disableOnLeftDownConfig.getAsBoolean() && input.button() == 0 && action == 1)
            disableTool("mouseLeftDown");
    }

    private boolean put(InGameManager manager, BlockPos blockPos, OperationSpeedLimit restockLimit) {
        restockLimit.costInteractBlock(); // 不管有没有成功，useItemOn一次都算是use了一次。另外这里同时也能立刻触发restock而避免放下“上一次拿着的方块”
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos.mutable(), false);
        return manager.useItemOn(interactionHand.getHand(), hit) == InteractionResult.SUCCESS;
    }
    private BlockPos currentPosition;//当前map区域的xyz值最小角坐标
    private int testDistance = -1;
    private int testSize = -1;
    private boolean @NotNull [] @NotNull [] @NotNull [] map = new boolean[0][][];
    private boolean @NotNull [] @NotNull [] @NotNull [] testBuffer = new boolean[0][][];
    private void resetTestBuffer(){
        for (boolean[][] bufferX : testBuffer) {
            for (boolean[] bufferXY : bufferX) {
                Arrays.fill(bufferXY, false);
            }
        }
    }
    private boolean getMapVec3i(@NotNull Vec3i pos){
        if(pos.getX() < 0 || pos.getX() >= testSize) return true;
        if(pos.getY() < 0 || pos.getY() >= testSize) return true;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return true;
        return map[pos.getX()][pos.getY()][pos.getZ()];
    }
    private void setMapVec3i(@NotNull Vec3i pos, boolean value){
        if(pos.getX() < 0 || pos.getX() >= testSize) return;
        if(pos.getY() < 0 || pos.getY() >= testSize) return;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return;
        map[pos.getX()][pos.getY()][pos.getZ()] = value;
    }
    private boolean getTestBufferVec3i(@NotNull Vec3i pos){
        if(pos.getX() < 0 || pos.getX() >= testSize) return true;
        if(pos.getY() < 0 || pos.getY() >= testSize) return true;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return true;
        return testBuffer[pos.getX()][pos.getY()][pos.getZ()];
    }
    @SuppressWarnings("SameParameterValue")
    private void setTestBufferVec3i(@NotNull Vec3i pos, boolean value){
        if(pos.getX() < 0 || pos.getX() >= testSize) return;
        if(pos.getY() < 0 || pos.getY() >= testSize) return;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return;
        testBuffer[pos.getX()][pos.getY()][pos.getZ()] = value;
    }
    private void initializeMap(@NotNull ShapeList shapeList, @NotNull BlockPos eyeBlockPos, @Nullable BlockGetter world){
        currentPosition = eyeBlockPos.offset(-testDistance, -testDistance, -testDistance);
        BlockPos pos1 = new BlockPos(currentPosition);
        for (boolean[][] mapX : map) {
            BlockPos pos2 = pos1;
            for (boolean[] mapXY : mapX) {
                BlockPos pos3 = pos2;
                for (int z = 0; z < mapXY.length; ++z) {
                    if(shapeList.testPos(pos3))
                        mapXY[z] = isUnpassable(pos3);
                    else mapXY[z] = outerRangeBlockMethod.get().isUnpassable(pos3, world);
                    pos3 = pos3.south();
                }
                pos2 = pos2.above();
            }
            pos1 = pos1.east();
        }
    }
    private boolean cantReach(Vec3i from, Vec3i to){
        //寻路，测试在已加载的map中从from点能否走到to点
        resetTestBuffer();
        Queue<Vec3i> searchQueue = new LinkedList<>();
        searchQueue.offer(from);
        while(!searchQueue.isEmpty()){
            Vec3i pos = searchQueue.poll();
            if(getMapVec3i(pos)) continue;
            int dstXZ = Math.abs(pos.getX() - to.getX()) + Math.abs(pos.getZ() - to.getZ());
            if(dstXZ <= 1){
                int dy = pos.getY() - to.getY();
                if(dy == 0 || dy == 1) return false;
                if(dy == -1 && !getTestBufferVec3i(pos.offset(0, 1, 0))) return false;
            }
            if(getTestBufferVec3i(pos)) continue;
            setTestBufferVec3i(pos, true);
            //y+
            if(!getMapVec3i(pos.offset(0, 2, 0)))
                searchQueue.offer(pos.offset(0, 1, 0));
            //y-
            searchQueue.offer(pos.offset(0, -1, 0));
            boolean hereLow = getMapVec3i(pos.offset(0, 1, 0));
            //x+
            if(hereLow || !getMapVec3i(pos.offset(1, 1, 0)))
                searchQueue.offer(pos.offset(1, 0, 0));
            //x-
            if(hereLow || !getMapVec3i(pos.offset(-1, 1, 0)))
                searchQueue.offer(pos.offset(-1, 0, 0));
            //z+
            if(hereLow || !getMapVec3i(pos.offset(0, 1, 1)))
                searchQueue.offer(pos.offset(0, 0, 1));
            //z-
            if(hereLow || !getMapVec3i(pos.offset(0, 1, -1)))
                searchQueue.offer(pos.offset(0, 0, -1));
        }
        return true;
    }
    private boolean canPut(Vec3i mapPos){
        if(getMapVec3i(mapPos)) return false;
        int nearStones = 0;
        if(getMapVec3i(mapPos.offset(1, 0, 0))) ++nearStones;
        if(getMapVec3i(mapPos.offset(-1, 0, 0))) ++nearStones;
        if(getMapVec3i(mapPos.offset(0, 1, 0))) ++nearStones;
        if(getMapVec3i(mapPos.offset(0, -1, 0))) ++nearStones;
        if(getMapVec3i(mapPos.offset(0, 0, 1))) ++nearStones;
        if(getMapVec3i(mapPos.offset(0, 0, -1))) ++nearStones;
        if(nearStones < 3) return false;
        if(nearStones >= 5) return true;
        setMapVec3i(mapPos, true);
        Vec3i[] positions = new Vec3i[13];
        int numPositions = 0;
        Vec3i test;
        test = mapPos.offset(1, 0, 0);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.offset(-1, 0, 0);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.offset(0, 0, 1);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.offset(0, 0, -1);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.offset(0, -1, 0);
        if(!getMapVec3i(test)){
            positions[numPositions++] = test;
            test = mapPos.offset(1, -1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(-1, -1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(0, -1, 1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(0, -1, -1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(0, -2, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
        }
        test = mapPos.offset(0, 1, 0);
        if(!getMapVec3i(test)){
            positions[numPositions++] = test;
            test = mapPos.offset(1, 1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(-1, 1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(0, 1, 1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(0, 1, -1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.offset(0, 2, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
        }
        int a;
        for(a = 0; a < numPositions; ++a) {
            int b;
            for (b = a + 1; b < numPositions; ++b) {
                if (cantReach(positions[a], positions[b])) break;
                if (cantReach(positions[b], positions[a])) break;
            }
            if (b != numPositions) break;
        }
        setMapVec3i(mapPos, false);
        return a == numPositions;
    }
    private boolean tryPut(InGameManager manager, BlockPos pos, OperationSpeedLimit restockLimit){
        if (!isReplaceable(pos)) return false;
        if (isUnpassable(pos)) return false;
        if (required(manager, pos)) return false;
        if (required(manager, pos.east())) return false;
        if (required(manager, pos.west())) return false;
        if (required(manager, pos.north())) return false;
        if (required(manager, pos.south())) return false;
        if (required(manager, pos.above())) return false;
        if (required(manager, pos.below())) return false;
        if (canPut(pos.subtract(currentPosition)))
            return put(manager, pos, restockLimit);
        return false;
    }

    @Override public void registerAll(boolean b) {
        Registries.END_CLIENT_TICK.register(this, b);
        Registries.IN_GAME_END_MOUSE.register(this, b);
    }
}