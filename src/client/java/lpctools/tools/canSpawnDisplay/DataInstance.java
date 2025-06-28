package lpctools.tools.canSpawnDisplay;

import lpctools.generic.GenericRegistry;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.util.AlgorithmUtils;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DataInstance implements AutoCloseable, Registries.ClientWorldChunkLightUpdated, ClientTickEvents.StartTick, ClientChunkEvents.Unload, Registries.ClientWorldChunkSetBlockState, GenericRegistry.SpawnConditionChanged, Registries.AfterClientWorldChange {
    public final HashMap<ChunkPos, ArrayList<BlockPos>> canSpawnPoses = new HashMap<>();
    public final @NotNull MinecraftClient client;
    protected void onChunkDataLoaded(ChunkPos pos, double distanceSquared){}
    protected void registerAll(boolean b){
        Registries.CLIENT_CHUNK_LIGHT_LOAD.register(this, b);
        Registries.START_CLIENT_TICK.register(this, b);
        Registries.CLIENT_CHUNK_UNLOAD.register(this, b);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
        Registries.AFTER_CLIENT_WORLD_CHANGE.register(this, b);
        GenericRegistry.SPAWN_CONDITION_CHANGED.register(this, b);
    }
    DataInstance(@NotNull MinecraftClient client){
        this.client = client;
        registerAll(true);
        if(client.world == null || client.player == null) return;
        Vec3d playerPos = client.player.getPos();
        retestData(client.world, playerPos);
    }
    public void clearData(){
        AlgorithmUtils.cancelTasks(updateTasks.values(), v->v);
        canSpawnPoses.clear();
    }
    public void retestData(@NotNull World world, @NotNull Vec3d playerPos){
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, playerPos))
            testChunkAsync(chunk, world.getLightingProvider(), chunk.getPos(), MathUtils.squaredDistance(playerPos, chunk.getPos()));
    }
    @Override public void onClientWorldChunkLightUpdated(@NotNull ClientWorld world, @NotNull WorldChunk chunk) {
        ChunkPos curr = chunk.getPos();
        double distanceSquared;
        if(client.player == null) distanceSquared = MathUtils.square(client.options.getViewDistance().getValue() * 16);
        else distanceSquared = MathUtils.squaredDistance(client.player.getEyePos(), curr);
        testChunkAsync(chunk, world.getLightingProvider(), new ChunkPos(curr.x, curr.z), distanceSquared);
    }
    @Override public void close() {
        clearData();
        registerAll(false);
    }
    @Override public void onStartTick(MinecraftClient mc) {
        AlgorithmUtils.consumeCompletedTasks(updateTasks, (pos, result)->{
            canSpawnPoses.put(pos, result.result);
            onChunkDataLoaded(pos, result.distanceSquared);
        });
        delayedUpdateChunks.forEach((pos, data)->{
            double squaredDistance = MathUtils.square(client.options.getViewDistance().getValue() * 16);
            testChunkAsync(data.chunk, data.lightingProvider, pos, squaredDistance);
        });
        delayedUpdateChunks.clear();
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        AlgorithmUtils.cancelTask(updateTasks.remove(chunk.getPos()));
        canSpawnPoses.remove(chunk.getPos());
    }
    @Override public void onSpawnConditionChanged() {
        if(client.world != null && client.player != null)
            retestData(client.world, client.player.getPos());
    }
    @Override public void afterWorldChange(MinecraftClient minecraftClient, ClientWorld clientWorld) {clearData();}
    
    private record DelayedUpdateData(Chunk chunk, LightingProvider lightingProvider){}
    private void tryPutDelayed(World world, int x, int z){
        Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, false);
        if(chunk == null) return;
        delayedUpdateChunks.put(new ChunkPos(x, z), new DelayedUpdateData(chunk, world.getLightingProvider()));
    }
    private final HashMap<ChunkPos, DelayedUpdateData> delayedUpdateChunks = new HashMap<>();
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState) {
        if(newState.getBlock() == Blocks.WATER) return;
        int x = pos.getX() >> 4, z = pos.getZ() >> 4;
        World world = chunk.getWorld();
        for(int dx = -1; dx <= 1; ++dx)
            for(int dz = -1; dz <= 1; ++dz)
                tryPutDelayed(world, x + dx, z + dz);
    }
    private void testChunkAsync(@NotNull Chunk chunk, @NotNull LightingProvider lightingProvider, @NotNull ChunkPos pos, double distanceSquared){
        AlgorithmUtils.cancelTask(updateTasks.remove(pos));
        updateTasks.put(pos, GenericUtils.supplyAsync(()->AsyncChunkTest(chunk, lightingProvider, pos, distanceSquared), distanceSquared));
    }
    private AsyncTestResult AsyncChunkTest(@NotNull Chunk chunk, @NotNull LightingProvider light, @NotNull ChunkPos pos, double distanceSquared){
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
    private final HashMap<ChunkPos, CompletableFuture<AsyncTestResult>> updateTasks = new HashMap<>();
    private record AsyncTestResult(ArrayList<BlockPos> result, double distanceSquared){}
}
