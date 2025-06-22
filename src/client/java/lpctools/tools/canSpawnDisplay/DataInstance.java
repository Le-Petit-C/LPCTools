package lpctools.tools.canSpawnDisplay;

import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.AlgorithmUtils;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

//TODO:setBlockState update
//TODO:SpawnConditionChanged update
//TODO:AfterClientWorldChange update
//TODO:OnScreenChange update (update shapeList)

public class DataInstance implements AutoCloseable, Registries.ClientWorldChunkLightUpdated, ClientTickEvents.StartTick, ClientChunkEvents.Unload, Registries.ClientWorldChunkSetBlockState{
    public final HashMap<ChunkPos, ArrayList<BlockPos>> canSpawnPoses;
    public final MinecraftClient client;
    protected void onChunkDataLoaded(ChunkPos pos, double distanceSquared){}
    protected void registerAll(boolean b){
        Registries.CLIENT_CHUNK_LIGHT_LOAD.register(this, b);
        Registries.START_CLIENT_TICK.register(this, b);
        Registries.CLIENT_CHUNK_UNLOAD.register(this, b);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
    }
    DataInstance(MinecraftClient client){
        this.client = client;
        canSpawnPoses = new HashMap<>();
        updateTasks = new HashMap<>();
        registerAll(true);
        if(client.world == null || client.player == null) return;
        Vec3d playerPos = client.player.getPos();
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(client.world, playerPos))
            testChunkAsync(client.world, chunk.getPos(), MathUtils.squaredDistance(playerPos, chunk.getPos()));
    }
    public void clear(){
        AlgorithmUtils.cancelTasks(updateTasks.values(), v->v);
        canSpawnPoses.clear();
    }
    @Override public void onClientWorldChunkLightUpdated(ClientWorld world, WorldChunk chunk) {
        ChunkPos curr = chunk.getPos();
        int viewDistance = client.options.getViewDistance().getValue();
        testChunkAsync(world, new ChunkPos(curr.x, curr.z), viewDistance * viewDistance * 256);
    }
    @Override public void close() {
        clear();
        registerAll(false);
    }
    @Override public void onStartTick(MinecraftClient mc) {
        ArrayList<ChunkPos> completedTasks = new ArrayList<>();
        for(Map.Entry<ChunkPos, CompletableFuture<AsyncTestResult>> task : updateTasks.entrySet()){
            if(!task.getValue().isDone()) continue;
            AsyncTestResult result = task.getValue().join();
            canSpawnPoses.put(task.getKey(), result.result);
            completedTasks.add(task.getKey());
            onChunkDataLoaded(task.getKey(), result.distanceSquared);
        }
        completedTasks.forEach(updateTasks::remove);
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        CompletableFuture<AsyncTestResult> task = updateTasks.remove(chunk.getPos());
        if(task != null){
            task.cancel(false);
            try{task.join();
            }catch (Exception ignored){}
        }
        canSpawnPoses.remove(chunk.getPos());
    }
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState) {
    
    }
    private void testChunkAsync(World world, ChunkPos pos, double distanceSquared){
        Chunk chunk = world.getChunk(pos.x, pos.z);
        updateTasks.put(pos, GenericUtils.supplyAsync(()->AsyncChunkTest(chunk, world.getLightingProvider(), pos, distanceSquared), distanceSquared));
    }
    private AsyncTestResult AsyncChunkTest(Chunk chunk, LightingProvider light, ChunkPos pos, double distanceSquared){
        AsyncTestResult result = new AsyncTestResult(new ArrayList<>(), distanceSquared);
        Iterable<BlockPos> blockPoses = AlgorithmUtils.iterateInBox(
            pos.x * 16, chunk.getBottomY(), pos.z * 16,
            pos.x * 16 + 15, chunk.getBottomY() + chunk.getHeight() - 1, pos.z * 16 + 15);
        for(BlockPos pos1 : blockPoses){
            if(GenericUtils.mayMobSpawnAt(chunk, light, pos1))
                result.result.add(new BlockPos(pos1.getX() & 0xf, pos1.getY(), pos1.getZ() & 0xf));
        }
        return result;
    }
    private final HashMap<ChunkPos, CompletableFuture<AsyncTestResult>> updateTasks;
    
    private record AsyncTestResult(ArrayList<BlockPos> result, double distanceSquared){}
}
