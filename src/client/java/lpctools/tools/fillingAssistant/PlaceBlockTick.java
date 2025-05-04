package lpctools.tools.fillingAssistant;

import lpctools.compact.derived.ShapeList;
import lpctools.lpcfymasaapi.Registry;
import lpctools.util.GuiUtils;
import lpctools.util.HandRestock;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static lpctools.tools.fillingAssistant.FillingAssistant.*;
import static lpctools.util.BlockUtils.*;

public class PlaceBlockTick implements ClientTickEvents.EndTick, Registry.InGameEndMouse {
    public PlaceBlockTick(){
        setTestDistance(testDistanceConfig.getAsInt());
    }
    public void setTestDistance(int distance){
        testDistance = distance;
        testSize = distance * 2 + 1;
        map = new boolean[testSize][testSize][testSize];
        testBuffer = new boolean[testSize][testSize][testSize];
    }

    @Override public void onEndTick(MinecraftClient client){
        if(client.world == null){
            disableTool("nullClientWorld");
            return;
        }
        if(client.player == null){
            disableTool("nullClientPlayerEntity");
            return;
        }
        ClientPlayerInteractionManager manager = client.interactionManager;
        if(manager == null){
            disableTool("nullInteractionManager");
            return;
        }
        if (manager.getCurrentGameMode() == GameMode.SPECTATOR || manager.getCurrentGameMode() == GameMode.ADVENTURE){
            disableTool("unsupportedGameMode");
            return;
        }
        if(disableOnGUIOpened.getAsBoolean() && GuiUtils.isInTextOrGui()){
            disableTool("GUIOpened");
            return;
        }
        HandRestock.IRestockTest restockTest = new HandRestock.SearchInSet(getPlaceableItems());
        if(HandRestock.search(restockTest, offhandFillingConfig.getAsBoolean() ? -1 : 0) == -1){//这个或许应该放在函数末尾，但是放在这里似乎也没什么坏处
            disableTool("placeableItemRanOut");
            return;
        }
        Vec3d eyePos = client.player.getEyePos();
        BlockPos eyeBlockPos = new BlockPos((int)Math.floor(eyePos.getX()), (int)Math.floor(eyePos.getY()), (int)Math.floor(eyePos.getZ()));
        boolean blockSetted;
        if(limitPlaceSpeedConfig.getAsBoolean()){
            if(canSetBlockCount > 1) canSetBlockCount = 0;
            canSetBlockCount += maxBlockPerTickConfig.getAsDouble();
        }
        else canSetBlockCount = Double.MAX_VALUE;
        ShapeList shapeList = limitFillingRange.buildShapeList();
        int r = (int) Math.ceil(reachDistanceConfig.getAsDouble());
        initializeMap(shapeList, eyeBlockPos);
        int startX = eyeBlockPos.getX() - r;
        int startY = eyeBlockPos.getY() - r;
        int startZ = eyeBlockPos.getZ() - r;
        int endX = eyeBlockPos.getX() + r;
        int endY = eyeBlockPos.getY() + r;
        int endZ = eyeBlockPos.getZ() + r;
        DimensionType dimensionType = client.world.getDimension();
        if(startY < dimensionType.minY()) startY = dimensionType.minY();
        if(endY > dimensionType.minY() + dimensionType.height() - 1) endY = dimensionType.minY() + dimensionType.height() - 1;
        do {
            blockSetted = false;
            for(BlockPos pos : BlockPos.iterate(startX, startY, startZ, endX, endY, endZ)){
                if(!shapeList.testPos(pos)) continue;
                Vec3d posD = pos.toCenterPos();
                if (posD.distanceTo(eyePos) >= reachDistanceConfig.getAsDouble()) continue;
                if(tryPut(pos, restockTest)){
                    if(isUnpassable(pos)){
                        setMapVec3i(pos.subtract(currentPosition), true);
                        blockSetted = true;
                        --canSetBlockCount;
                    }
                }
                if(canSetBlockCount < 1) return;
                if(!enabled) return;
            }
        }while(blockSetted);
    }
    @Override public void onInGameEndMouse(int button, int action, int mods) {
        if(disableOnLeftDownConfig.getAsBoolean() && button == 0 && action == 1)
            disableTool("mouseLeftDown");
    }

    private boolean put(BlockPos blockPos, HandRestock.IRestockTest restockTest){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.interactionManager == null) return false;
        if(!HandRestock.restock(restockTest, offhandFillingConfig.getAsBoolean() ? -1 : 0)){
            disableTool("placeableItemRanOut");
            return false;
        }
        BlockHitResult hit = new BlockHitResult(blockPos.toCenterPos(), Direction.UP, blockPos.mutableCopy(), false);
        return client.interactionManager.interactBlock(
                client.player,
                offhandFillingConfig.getAsBoolean() ? Hand.OFF_HAND : Hand.MAIN_HAND,
                hit
        ) == ActionResult.SUCCESS;
    }
    private BlockPos currentPosition;//当前map区域的xyz值最小角坐标
    private int testDistance;
    private int testSize;
    private boolean@NotNull [] @NotNull [] @NotNull [] map = new boolean[0][][];
    private boolean@NotNull [] @NotNull [] @NotNull [] testBuffer = new boolean[0][][];
    private double canSetBlockCount = 0;
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
    private void initializeMap(@NotNull ShapeList shapeList, @NotNull BlockPos eyeBlockPos){
        currentPosition = eyeBlockPos.add(-testDistance, -testDistance, -testDistance);
        BlockPos pos1 = new BlockPos(currentPosition);
        for (boolean[][] mapX : map) {
            BlockPos pos2 = pos1;
            for (boolean[] mapXY : mapX) {
                BlockPos pos3 = pos2;
                for (int z = 0; z < mapXY.length; ++z) {
                    if(shapeList.testPos(pos3))
                        mapXY[z] = isUnpassable(pos3);
                    else mapXY[z] = outerRangeBlockMethod.getCurrentUserdata().isUnpassable(pos3);
                    pos3 = pos3.south();
                }
                pos2 = pos2.up();
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
                if(dy == -1 && !getTestBufferVec3i(pos.add(0, 1, 0))) return false;
            }
            if(getTestBufferVec3i(pos)) continue;
            setTestBufferVec3i(pos, true);
            //y+
            if(!getMapVec3i(pos.add(0, 2, 0)))
                searchQueue.offer(pos.add(0, 1, 0));
            //y-
            searchQueue.offer(pos.add(0, -1, 0));
            boolean hereLow = getMapVec3i(pos.add(0, 1, 0));
            //x+
            if(hereLow || !getMapVec3i(pos.add(1, 1, 0)))
                searchQueue.offer(pos.add(1, 0, 0));
            //x-
            if(hereLow || !getMapVec3i(pos.add(-1, 1, 0)))
                searchQueue.offer(pos.add(-1, 0, 0));
            //z+
            if(hereLow || !getMapVec3i(pos.add(0, 1, 1)))
                searchQueue.offer(pos.add(0, 0, 1));
            //z-
            if(hereLow || !getMapVec3i(pos.add(0, 1, -1)))
                searchQueue.offer(pos.add(0, 0, -1));
        }
        return true;
    }
    private boolean canPut(Vec3i mapPos){
        if(getMapVec3i(mapPos)) return false;
        int nearStones = 0;
        if(getMapVec3i(mapPos.add(1, 0, 0))) ++nearStones;
        if(getMapVec3i(mapPos.add(-1, 0, 0))) ++nearStones;
        if(getMapVec3i(mapPos.add(0, 1, 0))) ++nearStones;
        if(getMapVec3i(mapPos.add(0, -1, 0))) ++nearStones;
        if(getMapVec3i(mapPos.add(0, 0, 1))) ++nearStones;
        if(getMapVec3i(mapPos.add(0, 0, -1))) ++nearStones;
        if(nearStones < 3) return false;
        if(nearStones >= 5) return true;
        setMapVec3i(mapPos, true);
        Vec3i[] positions = new Vec3i[13];
        int numPositions = 0;
        Vec3i test;
        test = mapPos.add(1, 0, 0);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.add(-1, 0, 0);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.add(0, 0, 1);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.add(0, 0, -1);
        if(!getMapVec3i(test)) positions[numPositions++] = test;
        test = mapPos.add(0, -1, 0);
        if(!getMapVec3i(test)){
            positions[numPositions++] = test;
            test = mapPos.add(1, -1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(-1, -1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(0, -1, 1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(0, -1, -1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(0, -2, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
        }
        test = mapPos.add(0, 1, 0);
        if(!getMapVec3i(test)){
            positions[numPositions++] = test;
            test = mapPos.add(1, 1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(-1, 1, 0);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(0, 1, 1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(0, 1, -1);
            if(!getMapVec3i(test)) positions[numPositions++] = test;
            test = mapPos.add(0, 2, 0);
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
    private boolean tryPut(BlockPos pos, HandRestock.IRestockTest restockTest){
        if (!isReplaceable(pos)) return false;
        if (isUnpassable(pos)) return false;
        if (required(pos)) return false;
        if (required(pos.east())) return false;
        if (required(pos.west())) return false;
        if (required(pos.north())) return false;
        if (required(pos.south())) return false;
        if (required(pos.up())) return false;
        if (required(pos.down())) return false;
        if (canPut(pos.subtract(currentPosition)))
            return put(pos, restockTest);
        return false;
    }
}