package cn.edge_filler.mixin.client;

import cn.edge_filler.Data;
import cn.edge_filler.HandRestock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

@Mixin(ClientPlayerEntity.class)
public class PlaceBlockTick {
    @Shadow @Final protected MinecraftClient client;

    @Unique
    private Data.BLOCKTYPE blockType(BlockPos blockPosToCheck) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.world == null) return Data.BLOCKTYPE.ERROR;
        Block block = client.world.getBlockState(blockPosToCheck).getBlock();
        if(block == Blocks.AIR
                || block == Blocks.CAVE_AIR
                || block == Blocks.VINE
                || block == Blocks.VOID_AIR
                || block == Blocks.WATER
                || block == Blocks.LAVA
                || block == Blocks.TALL_GRASS
                || block == Blocks.SHORT_GRASS
                || block == Blocks.GLOW_LICHEN
                || block == Blocks.SEAGRASS
                || block == Blocks.TALL_SEAGRASS
                || block == Blocks.BUBBLE_COLUMN
                || block == Blocks.SNOW
        ){
            return Data.BLOCKTYPE.EMPTY;
        }
        else if(block == Blocks.STONE
                || block == Blocks.DEEPSLATE
                || block == Blocks.GRAVEL
                || block == Blocks.TUFF
                || block == Blocks.DIORITE
                || block == Blocks.GRANITE
                || block == Blocks.ANDESITE
                || block == Blocks.DIRT
                || block == Blocks.GRASS_BLOCK
                || block == Blocks.CLAY
                || block == Blocks.MAGMA_BLOCK
                || block == Blocks.SAND
                || block == Blocks.SMOOTH_BASALT
                || block == Blocks.BEDROCK
                || block == Blocks.OBSIDIAN
                || block == Blocks.NETHERRACK
                || block == Blocks.COBBLESTONE
                || block == Blocks.COBBLED_DEEPSLATE
                || block == Blocks.MOSS_BLOCK
                || block == Blocks.ROOTED_DIRT
                || block == Blocks.SANDSTONE
                || block == Blocks.WAXED_COPPER_BLOCK
                || block == Blocks.WAXED_OXIDIZED_COPPER
                || block == Blocks.WAXED_OXIDIZED_CUT_COPPER
                || block == Blocks.COARSE_DIRT
                || block == Blocks.PODZOL
        ){
            return Data.BLOCKTYPE.STONE;
        }
        else return Data.BLOCKTYPE.OTHERS;
    }

    @Unique
    Boolean put(BlockPos blockpos){
        if(client.interactionManager == null) return false;
        HandRestock.restock();
        if(!Data.shouldplace) return false;
        Vec3d pos = new Vec3d(blockpos.getX() + 0.5, blockpos.getY() + 0.5, blockpos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(pos, Direction.UP, blockpos, true);
        return client.interactionManager.interactBlock(client.player, Hand.MAIN_HAND, hit) == ActionResult.SUCCESS;
    }
    @Unique
    private BlockPos currentPosition;//当前map区域的xyz值最小角坐标
    @Unique
    private int testDistance = 5;
    @Unique
    private int testSize = 11;
    @Unique
    private boolean[][][] map = new boolean[testSize][testSize][testSize];
    @Unique
    private boolean[][][] testBuffer = new boolean[testSize][testSize][testSize];
    @Unique
    public void setTestDistance(int distance){
        testDistance = distance;
        testSize = distance * 2 + 1;
        map = new boolean[testSize][testSize][testSize];
        testBuffer = new boolean[testSize][testSize][testSize];
    }
    @Unique
    private void resetTestBuffer(){
        for (boolean[][] bufferX : testBuffer) {
            for (boolean[] bufferXY : bufferX) {
                Arrays.fill(bufferXY, false);
            }
        }
    }
    @Unique
    boolean getMapVec3i(@NotNull Vec3i pos){
        if(pos.getX() < 0 || pos.getX() >= testSize) return true;
        if(pos.getY() < 0 || pos.getY() >= testSize) return true;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return true;
        return map[pos.getX()][pos.getY()][pos.getZ()];
    }
    @Unique
    void setMapVec3i(@NotNull Vec3i pos, boolean value){
        if(pos.getX() < 0 || pos.getX() >= testSize) return;
        if(pos.getY() < 0 || pos.getY() >= testSize) return;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return;
        map[pos.getX()][pos.getY()][pos.getZ()] = value;
    }
    @Unique
    boolean getTestBufferVec3i(@NotNull Vec3i pos){
        if(pos.getX() < 0 || pos.getX() >= testSize) return true;
        if(pos.getY() < 0 || pos.getY() >= testSize) return true;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return true;
        return testBuffer[pos.getX()][pos.getY()][pos.getZ()];
    }
    @Unique
    void setTestBufferVec3i(@NotNull Vec3i pos, boolean value){
        if(pos.getX() < 0 || pos.getX() >= testSize) return;
        if(pos.getY() < 0 || pos.getY() >= testSize) return;
        if(pos.getZ() < 0 || pos.getZ() >= testSize) return;
        testBuffer[pos.getX()][pos.getY()][pos.getZ()] = value;
    }
    @Unique
    void initializeMap(@NotNull BlockPos eyeBlockPos){
        currentPosition = eyeBlockPos.add(-testDistance, -testDistance, -testDistance);
        BlockPos pos1 = new BlockPos(currentPosition);
        for (boolean[][] mapX : map) {
            BlockPos pos2 = pos1;
            for (boolean[] mapXY : mapX) {
                BlockPos pos3 = pos2;
                for (int z = 0; z < mapXY.length; ++z) {
                    mapXY[z] = (blockType(pos3) == Data.BLOCKTYPE.STONE);
                    pos3 = pos3.south();
                }
                pos2 = pos2.up();
            }
            pos1 = pos1.east();
        }
    }
    @Unique
    boolean cantReach(Vec3i from, Vec3i to){
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
    @Unique
    Boolean canPut(Vec3i mapPos){
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

    @Unique
    Boolean tryPut(BlockPos pos){
        if(canPut(pos.subtract(currentPosition)))
            return put(pos);
        return false;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci){
        if(!Data.shouldplace || client . player == null) return;
        Vec3d eyePos = client.player.getEyePos();
        BlockPos eyeBlockPos = new BlockPos((int)Math.floor(eyePos.getX()), (int)Math.floor(eyePos.getY()), (int)Math.floor(eyePos.getZ()));
        boolean loop;
        initializeMap(eyeBlockPos);
        do {
            loop = false;
            for (int y = -4; y <= 4; ++y) {
                BlockPos p1 = eyeBlockPos.add(0, y, 0);
                if (p1.getY() < -64 || p1.getY() >= 320) continue;
                for (int x = -4; x <= 4; ++x) {
                    BlockPos p2 = p1.add(x, 0, 0);
                    for (int z = -4; z <= 4; ++z) {
                        BlockPos pos = p2.add(0, 0, z);
                        Vec3d posD = new Vec3d(pos.getX(), pos.getY(), pos.getZ());
                        if (posD.distanceTo(eyePos) >= 4.0) continue;
                        if(x == 0 && z == 0 && (y == 0 || y == -1)) continue;
                        if (blockType(pos) == Data.BLOCKTYPE.EMPTY) {
                            if(tryPut(pos)){
                                loop = true;
                                initializeMap(eyeBlockPos);
                                //map[testDistance - x][testDistance - y][testDistance - z] = true;
                            }
                        }
                    }
                }
            }
        }while(loop);
    }
}