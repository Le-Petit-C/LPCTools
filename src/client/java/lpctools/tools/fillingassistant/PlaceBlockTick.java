package lpctools.tools.fillingassistant;

import lpctools.lpcfymasaapi.LPCAPIInit;
import lpctools.lpcfymasaapi.Registry;
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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import static lpctools.tools.fillingassistant.FillingAssistant.*;

public class PlaceBlockTick implements ClientTickEvents.EndTick, Registry.InGameEndMouse {
    public PlaceBlockTick(){
        setTestDistance(testDistanceConfig.getValue());
    }
    public void setTestDistance(int distance){
        testDistance = distance;
        testSize = distance * 2 + 1;
        map = new boolean[testSize][testSize][testSize];
        testBuffer = new boolean[testSize][testSize][testSize];
    }

    @Override public void onEndTick(MinecraftClient client){
        if(client.player == null){
            disableTool("lpctools.tools.fillingAssistant.disableReason.nullClientPlayerEntity");
            return;
        }
        ClientPlayerInteractionManager manager = client.interactionManager;
        if(manager == null){
            disableTool("lpctools.tools.fillingAssistant.disableReason.nullInteractionManager");
            return;
        }
        if (manager.getCurrentGameMode() == GameMode.SPECTATOR || manager.getCurrentGameMode() == GameMode.ADVENTURE){
            disableTool("lpctools.tools.fillingAssistant.disableReason.unsupportedGameMode");
            return;
        }
        if(disableOnGUIOpened.getValue() && LPCAPIInit.isInTextOrGui()){
            disableTool("lpctools.tools.fillingAssistant.disableReason.GUIOpened");
            return;
        }
        if(HandRestock.search(getPlaceableItems()) == -1){//这个或许应该放在函数末尾，但是放在这里似乎也没什么坏处
            disableTool("lpctools.tools.fillingAssistant.disableReason.placeableItemRanOut");
            return;
        }
        Vec3d eyePos = client.player.getEyePos();
        BlockPos eyeBlockPos = new BlockPos((int)Math.floor(eyePos.getX()), (int)Math.floor(eyePos.getY()), (int)Math.floor(eyePos.getZ()));
        boolean blockSetted;
        if(limitPlaceSpeedConfig.getValue()){
            if(canSetBlockCount > 1) canSetBlockCount = 0;
            canSetBlockCount += maxBlockPerTickConfig.getValue();
        }
        else canSetBlockCount = 65536;
        initializeMap(eyeBlockPos);
        do {
            blockSetted = false;
            int y;
            for (y = -4; y <= 4; ++y) {
                BlockPos p1 = eyeBlockPos.add(0, y, 0);
                if (p1.getY() < -64 || p1.getY() >= 320) continue;
                int x;
                for (x = -4; x <= 4; ++x) {
                    BlockPos p2 = p1.add(x, 0, 0);
                    int z;
                    for (z = -4; z <= 4; ++z) {
                        BlockPos pos = p2.add(0, 0, z);
                        Vec3d posD = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        if (posD.distanceTo(eyePos) >= reachDistanceConfig.getValue()) continue;
                        if(x == 0 && z == 0 && (y == 0 || y == -1)) continue;
                        if(tryPut(pos)){
                            if(unpassable(pos)){
                                setMapVec3i(pos.subtract(currentPosition), true);
                                blockSetted = true;
                                --canSetBlockCount;
                            }
                        }
                        if(canSetBlockCount < 1) break;
                        if(!enabled()) break;
                    }
                    if(z != 5) break;
                }
                if(x != 5) break;
            }
            if(y != 5) break;
        }while(blockSetted);
    }
    @Override public void onInGameEndMouse(int button, int action, int mods) {
        if(disableOnLeftDownConfig.getValue() && button == 0 && action == 1)
            disableTool("lpctools.tools.fillingAssistant.disableReason.mouseLeftDown");
    }

    private boolean put(BlockPos blockpos){
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.interactionManager == null) return false;
        if(!HandRestock.restock(getPlaceableItems())){
            disableTool("lpctools.tools.fillingAssistant.disableReason.placeableItemRanOut");
            return false;
        }
        Vec3d pos = new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(pos, Direction.UP, blockpos, false);
        return client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit) == ActionResult.SUCCESS;
    }
    private BlockPos currentPosition;//当前map区域的xyz值最小角坐标
    private int testDistance;
    private int testSize;
    private boolean@NotNull [] @NotNull [] @NotNull [] map = new boolean[0][][];
    private boolean@NotNull [] @NotNull [] @NotNull [] testBuffer = new boolean[0][][];
    private double canSetBlockCount;
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
    private void setTestBufferVec3i(@NotNull Vec3i pos, boolean value){
        if(pos.getX() < 0 || pos.getX() >= testSize) return;
        if(pos.getY() < 0 || pos.getY() >= testSize) return;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return;
        testBuffer[pos.getX()][pos.getY()][pos.getZ()] = value;
    }
    private void initializeMap(@NotNull BlockPos eyeBlockPos){
        currentPosition = eyeBlockPos.add(-testDistance, -testDistance, -testDistance);
        BlockPos pos1 = new BlockPos(currentPosition);
        for (boolean[][] mapX : map) {
            BlockPos pos2 = pos1;
            for (boolean[] mapXY : mapX) {
                BlockPos pos3 = pos2;
                for (int z = 0; z < mapXY.length; ++z) {
                    mapXY[z] = unpassable(pos3);
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
    private boolean tryPut(BlockPos pos){
        if (unpassable(pos)) return false;
        if (required(pos)) return false;
        if (required(pos.east())) return false;
        if (required(pos.west())) return false;
        if (required(pos.north())) return false;
        if (required(pos.south())) return false;
        if (required(pos.up())) return false;
        if (required(pos.down())) return false;
        if (canPut(pos.subtract(currentPosition)))
            return put(pos);
        return false;
    }
}