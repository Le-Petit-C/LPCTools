package lpctools.tools.slightXRay;

import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.AlgorithmUtils;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

import static lpctools.tools.slightXRay.SlightXRayData.*;
import static lpctools.util.AlgorithmUtils.iterateInManhattanDistance;
import static lpctools.util.BlockUtils.isFluid;

public class DataInstance implements AutoCloseable, ClientChunkEvents.Load, ClientChunkEvents.Unload, ClientWorldEvents.AfterClientWorldChange, Registries.ClientWorldChunkSetBlockState, ClientTickEvents.StartTick {
    public final HashMap<ChunkPos, HashMap<BlockPos, MutableInt>> markedPoses;
    protected void onXRayChunkUpdated(ChunkPos pos, double distanceSquare){}
    @Override public void onStartTick(MinecraftClient mc) {
        HashSet<ChunkPos> completedFutures = new HashSet<>();
        for(UpdateData data : updateFutures){
            if(!data.future.isDone()) continue;
            completedFutures.add(data.pos);
            HashMap<BlockPos, MutableInt> result = data.future.join();
            markedPoses.put(data.pos, result);
            onXRayChunkUpdated(data.pos, data.distanceSquare);
        }
        AlgorithmUtils.fastRemove(updateFutures, v->completedFutures.contains(v.pos));
    }
    public final MinecraftClient client;
    public double squaredDistanceByClient(ChunkPos chunkPos){
        if(client.player != null) return MathUtils.squaredDistance(client.player.getEyePos(), chunkPos);
        else return MathUtils.square(client.options.getViewDistance().getValue() * 16);
    }
    DataInstance(MinecraftClient client){
        this.client = client;
        markedPoses = new HashMap<>();
        Registries.AFTER_CLIENT_WORLD_CHANGE.register(this);
        Registries.START_CLIENT_TICK.register(this);
        Registries.CLIENT_CHUNK_LOAD.register(this);
        Registries.CLIENT_CHUNK_UNLOAD.register(this);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this);
        resetData();
    }
    public void resetData(){
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if(player == null || world == null) return;
        Vec3d playerEyePos = player.getEyePos();
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, playerEyePos)){
            ChunkPos chunkPos = chunk.getPos();
            testChunkAsync(world, chunkPos, MathUtils.squaredDistance(playerEyePos, chunkPos));
        }
    }
    @Override public void close(){
        Registries.AFTER_CLIENT_WORLD_CHANGE.unregister(this);
        Registries.START_CLIENT_TICK.unregister(this);
        Registries.CLIENT_CHUNK_LOAD.unregister(this);
        Registries.CLIENT_CHUNK_UNLOAD.unregister(this);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.unregister(this);
        clearData();
    }
    @Override public void onChunkLoad(ClientWorld world, WorldChunk chunk) {
        ChunkPos pos = chunk.getPos();
        double distanceSquare;
        if(client.player != null) distanceSquare = MathUtils.squaredDistance(client.player.getEyePos(), chunk.getPos());
        else distanceSquare = MathUtils.square(client.options.getViewDistance().getValue() * 16);
        testChunkAsync(world, pos, distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x - 1, pos.z), distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x + 1, pos.z), distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x, pos.z - 1), distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x, pos.z + 1), distanceSquare);
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        markedPoses.remove(chunkPos);
        onXRayChunkUpdated(chunkPos, squaredDistanceByClient(chunkPos));
    }
    @Override public void afterWorldChange(MinecraftClient mc, ClientWorld world) {clearData();}
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState) {
        if(newState == null) newState = Blocks.AIR.getDefaultState();
        if(isFluid(newState.getBlock())) return;
        if(doShowAround(newState)){
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2))
                testPos(chunk.getWorld(), pos1);
        }
        else testPos(chunk.getWorld(), pos);
    }
    public @Nullable MutableInt get(BlockPos pos){
        HashMap<BlockPos, MutableInt> map = markedPoses.get(new ChunkPos(pos));
        return map != null ? map.get(pos) : null;
    }
    protected void clearData(){
        AlgorithmUtils.cancelTasks(updateFutures, v->v.future);
        markedPoses.clear();
    }
    private void removePos(BlockPos pos){
        HashMap<BlockPos, MutableInt> map = markedPoses.get(new ChunkPos(pos));
        if(map != null) map.remove(pos);
    }
    private void putPos(BlockPos pos, MutableInt color){
        markedPoses.computeIfAbsent(new ChunkPos(pos), pos1->new HashMap<>()).put(pos.toImmutable(), color);
    }
    private void testPos(World world, BlockPos pos){
        BlockState state = world.getBlockState(pos);
        MutableInt color = XRayBlocks.get(state.getBlock());
        if(color == null){removePos(pos); return;}
        for(BlockPos pos1 : iterateInManhattanDistance(pos, 2)) {
            if(doShowAround(world.getBlockState(pos1))){
                putPos(pos, color);
                return;
            }
        }
        removePos(pos);
    }
    private static boolean doShowAround(BlockState state){
        return !state.isOpaque() || state.isTransparent();
    }
    private record UpdateData(ChunkPos pos, CompletableFuture<HashMap<BlockPos, MutableInt>> future, double distanceSquare){}
    private final ArrayList<UpdateData> updateFutures = new ArrayList<>();
    private void testChunkAsync(ClientWorld world, ChunkPos pos, double distanceSquare){
        ChunkTask task = ChunkTask.buildTask(world, pos);
        if(task != null) {
            HashMap<Block, MutableInt> copy;
            synchronized (XRayBlocks){copy = new HashMap<>(XRayBlocks);}
            updateFutures.add(new UpdateData(pos, GenericUtils.supplyAsync(()->task.testCurrentChunk(copy), distanceSquare), distanceSquare));
        }
    }
    
    private record ChunkTask(ChunkPos pos, Chunk current, Chunk west, Chunk east, Chunk north, Chunk south){
        public static @Nullable DataInstance.ChunkTask buildTask(ClientWorld world, ChunkPos pos){
            ChunkTask task = new ChunkTask(pos,
                world.getChunk(pos.x, pos.z, ChunkStatus.FULL, false),
                world.getChunk(pos.x - 1, pos.z, ChunkStatus.FULL, false),
                world.getChunk(pos.x + 1, pos.z, ChunkStatus.FULL, false),
                world.getChunk(pos.x, pos.z - 1, ChunkStatus.FULL, false),
                world.getChunk(pos.x, pos.z + 1, ChunkStatus.FULL, false));
            if(task.current == null || task.west == null || task.east == null || task.north == null || task.south == null)
                return null;
            return task;
        }
        public HashMap<BlockPos, MutableInt> testCurrentChunk(HashMap<Block, MutableInt> colorMap){
            HashMap<BlockPos, MutableInt> res = new HashMap<>();
            int bottom = current.getBottomY(), height = current.getHeight(), top = bottom + height;
            TestData displaysNear = new TestData(bottom, height);
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, 0, 15, top - 1, 15))
                displaysNear.set(pos1, doShowAround(current.getBlockState(pos1)));
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(-1, bottom, 0, -1, top - 1, 15))
                displaysNear.set(pos1, doShowAround(west.getBlockState(pos1)));
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(16, bottom, 0, 16, top - 1, 15))
                displaysNear.set(pos1, doShowAround(east.getBlockState(pos1)));
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, -1, 15, top - 1, -1))
                displaysNear.set(pos1, doShowAround(north.getBlockState(pos1)));
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, 16, 15, top - 1, 16))
                displaysNear.set(pos1, doShowAround(south.getBlockState(pos1)));
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom - 1, 0, 15, bottom - 1, 15))
                displaysNear.set(pos1, true);
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, top, 0, 15, top, 15))
                displaysNear.set(pos1, true);
            for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, 0, 15, top - 1, 15)){
                BlockState state = current.getBlockState(pos1);
                MutableInt color = colorMap.get(state.getBlock());
                if(color == null) continue;
                if(displaysNear.get(pos1)){res.put(pos1.add(pos.getStartPos()).toImmutable(), color); continue;}
                for(Direction direction : Direction.values())
                    if(displaysNear.get(pos1.offset(direction))){
                        res.put(pos1.add(pos.getStartPos()).toImmutable(), color); break;}
            }
            return res;
        }
        private static class TestData{
            public final boolean[][][] data;
            public final int bottomY, worldHeight;
            TestData(int bottomY, int worldHeight){
                this.bottomY = bottomY; this.worldHeight = worldHeight;
                data = new boolean[18][worldHeight + 2][18];
            }
            boolean get(BlockPos pos){return data[pos.getX() + 1][pos.getY() - bottomY + 1][pos.getZ() + 1];}
            void set(BlockPos pos, boolean value){data[pos.getX() + 1][pos.getY() - bottomY + 1][pos.getZ() + 1] = value;}
        }
    }
}
