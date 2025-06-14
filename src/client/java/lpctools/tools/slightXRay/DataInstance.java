package lpctools.tools.slightXRay;

import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.UnregistrableRegistry;
import lpctools.util.AlgorithmUtils;
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
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static lpctools.util.AlgorithmUtils.iterateInManhattanDistance;
import static lpctools.util.BlockUtils.isFluid;

public class DataInstance implements AutoCloseable, ClientChunkEvents.Load, ClientChunkEvents.Unload, ClientWorldEvents.AfterClientWorldChange, Registries.ClientWorldChunkSetBlockState, ClientTickEvents.StartTick {
    public final HashMap<ChunkPos, HashMap<BlockPos, MutableInt>> markedPoses;
    public final UnregistrableRegistry<OnXRayChunkUpdated> ON_XRAY_CHUNK_UPDATED = new UnregistrableRegistry<>(
        callbacks->(pos, markedPoses)->callbacks.forEach(callback->callback.onXRayChunkUpdated(pos, markedPoses))
    );
    @Override public void onStartTick(MinecraftClient mc) {
        ArrayList<ChunkPos> completedFutures = new ArrayList<>();
        for(Map.Entry<ChunkPos, CompletableFuture<HashMap<BlockPos, MutableInt>>> entry : updateFutures.entrySet()){
            if(!entry.getValue().isDone()) continue;
            completedFutures.add(entry.getKey());
            HashMap<BlockPos, MutableInt> result = entry.getValue().join();
            markedPoses.put(entry.getKey(), result);
            ON_XRAY_CHUNK_UPDATED.run().onXRayChunkUpdated(entry.getKey(), result);
        }
        completedFutures.forEach(updateFutures::remove);
    }
    public interface OnXRayChunkUpdated{
        void onXRayChunkUpdated(ChunkPos pos, HashMap<BlockPos, MutableInt> markedPoses);
    }
    MinecraftClient client;
    DataInstance(MinecraftClient client){
        this.client = client;
        markedPoses = new HashMap<>();
        Registries.START_CLIENT_TICK.register(this);
        Registries.CLIENT_CHUNK_LOAD.register(this);
        Registries.CLIENT_CHUNK_UNLOAD.register(this);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this);
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if(world != null && player != null) addAllRegionsIntoWork(world, player);
    }
    public void reset(ClientWorld world, ClientPlayerEntity player){
        clearAll();
        addAllRegionsIntoWork(world, player);
    }
    @Override public void close(){
        Registries.START_CLIENT_TICK.unregister(this);
        Registries.CLIENT_CHUNK_LOAD.unregister(this);
        Registries.CLIENT_CHUNK_UNLOAD.unregister(this);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.unregister(this);
        clearAll();
    }
    @Override public void onChunkLoad(ClientWorld world, WorldChunk chunk) {
        ChunkPos pos = chunk.getPos();
        int viewDistance = client.options.getViewDistance().getValue();
        double priority = viewDistance * viewDistance * 256.0;
        testChunkAsync(world, pos, priority);
        testChunkAsync(world, new ChunkPos(pos.x - 1, pos.z), priority);
        testChunkAsync(world, new ChunkPos(pos.x + 1, pos.z), priority);
        testChunkAsync(world, new ChunkPos(pos.x, pos.z - 1), priority);
        testChunkAsync(world, new ChunkPos(pos.x, pos.z + 1), priority);
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        markedPoses.remove(chunk.getPos());
    }
    @Override public void afterWorldChange(MinecraftClient mc, ClientWorld world) {clearAll();}
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
    
    private void addAllRegionsIntoWork(ClientWorld world, ClientPlayerEntity player){
        ChunkPos chunkPos = player.getChunkPos();
        Vec3d playerPos = player.getPos();
        for(Vector2i vec : AlgorithmUtils.iterateFromClosestInDistance(new Vector2i(chunkPos.x, chunkPos.z), client.options.getViewDistance().getValue()))
            testChunkAsync(world, new ChunkPos(vec.x, vec.y), playerPos.distanceTo(new Vec3d(vec.x * 16 + 8.0, playerPos.y, vec.y * 16 + 8.0)));
    }
    private void clearAll(){
        updateFutures.values().forEach(future->future.cancel(false));
        try {CompletableFuture.allOf(updateFutures.values().toArray(new CompletableFuture<?>[0])).join();
        }catch (Exception ignored){}
        updateFutures.clear();
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
        MutableInt color = SlightXRay.XRayBlocks.get(state.getBlock());
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
    private final HashMap<ChunkPos, CompletableFuture<HashMap<BlockPos, MutableInt>>> updateFutures = new HashMap<>();
    private void testChunkAsync(ClientWorld world, ChunkPos pos, double priority){
        ChunkTask task = ChunkTask.buildTask(world, pos);
        if(task != null) {
            HashMap<Block, MutableInt> XRayBlocks;
            synchronized (SlightXRay.XRayBlocks){XRayBlocks = new HashMap<>(SlightXRay.XRayBlocks);}
            updateFutures.put(pos, GenericUtils.supplyAsync(()->task.testCurrentChunk(XRayBlocks), priority));
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
