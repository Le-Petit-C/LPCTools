package lpctools.tools.canSpawnDisplay;

import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.AlgorithmUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
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

public class DataInstance implements AutoCloseable, Registries.ClientWorldChunkLightUpdated, ClientTickEvents.StartTick, ClientChunkEvents.Unload{
    public final HashMap<ChunkPos, ArrayList<BlockPos>> canSpawnPoses;
    DataInstance(World world, Vec3d playerPos){
        canSpawnPoses = new HashMap<>();
        updateTasks = new HashMap<>();
        Registries.CLIENT_CHUNK_LIGHT_LOAD.register(this);
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, playerPos))
            testChunkAsync(world, chunk.getPos());
    }
    public void clear(){
        AlgorithmUtils.cancelTasks(updateTasks.values(), v->v);
        canSpawnPoses.clear();
    }
    @Override public void onClientWorldChunkLightUpdated(WorldChunk chunk) {
        World world = chunk.getWorld();
        ChunkPos curr = chunk.getPos();
        testChunkAsync(world, new ChunkPos(curr.x, curr.z));
    }
    @Override public void close() {
        clear();
        Registries.CLIENT_CHUNK_LIGHT_LOAD.unregister(this);
    }
    @Override public void onStartTick(MinecraftClient mc) {
        ArrayList<ChunkPos> completedTasks = new ArrayList<>();
        for(Map.Entry<ChunkPos, CompletableFuture<AsyncTestResult>> task : updateTasks.entrySet()){
            if(!task.getValue().isDone()) continue;
            AsyncTestResult result = task.getValue().join();
            canSpawnPoses.put(task.getKey(), result.result);
            completedTasks.add(task.getKey());
        }
        completedTasks.forEach(updateTasks::remove);
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        updateTasks.remove(chunk.getPos()).cancel(false);
        canSpawnPoses.remove(chunk.getPos());
    }
    private void testChunkAsync(World world, ChunkPos pos){
        Chunk chunk = world.getChunk(pos.x, pos.z);
        updateTasks.put(pos, GenericUtils.supplyAsync(()->AsyncChunkTest(chunk, world.getLightingProvider(), pos)));
    }
    private AsyncTestResult AsyncChunkTest(Chunk chunk, LightingProvider light, ChunkPos pos){
        AsyncTestResult result = new AsyncTestResult(new ArrayList<>());
        Iterable<BlockPos> blockPoses = AlgorithmUtils.iterateInBox(
            pos.x * 16, chunk.getBottomY(), pos.z * 16,
            pos.x * 16 + 15, chunk.getBottomY() + chunk.getHeight() - 1, pos.z * 16 + 15);
        for(BlockPos pos1 : blockPoses){
            if(GenericUtils.mayMobSpawnAt(chunk, light, pos1))
                result.result.add(pos1);
        }
        return result;
    }
    private final HashMap<ChunkPos, CompletableFuture<AsyncTestResult>> updateTasks;
    private record AsyncTestResult(ArrayList<BlockPos> result){}
}
