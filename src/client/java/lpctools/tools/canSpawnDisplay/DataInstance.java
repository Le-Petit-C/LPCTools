package lpctools.tools.canSpawnDisplay;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericConfigs;
import lpctools.generic.GenericRegistry;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeReference;
import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.LPCMathHelper;
import lpctools.util.Packed;
import lpctools.util.data.minecraft.CombinedBlockGetters;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLevelEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static lpctools.tools.ToolUtils.clearMapDataOutOfRange;

public class DataInstance implements AutoCloseable, Registries.ClientWorldChunkLightUpdated, ClientChunkEvents.Load, Registries.BetweenRenderFrames, Registries.ClientWorldChunkSetBlockState, GenericRegistry.SpawnConditionChanged, ClientLevelEvents.AfterClientLevelChange {
    
    private record DelayedTask(long packedChunkPos, Supplier<RunningTask> task){}
    private record RunningTask(long packedChunkPos, CompletableFuture<TaskResult> task){}
    private record TaskResult(long packedChunkPos, ArrayList<BlockPos> result){}
    
    public final Long2ObjectOpenHashMap<HashMap<BlockPos, ShapeReference>> canSpawnPoses = new Long2ObjectOpenHashMap<>();
    public final @NotNull Minecraft client;
    
    private final ArrayList<RunningTask> runningTasks = new ArrayList<>();
    private final ArrayList<DelayedTask> delayedTasks = new ArrayList<>();
    private final int runningTasksLimit = Runtime.getRuntime().availableProcessors();
    
    private int updateCounter = 0;
    private int renderColor;
    private IRenderMethod method;
    private ICSShapeRegister shapeRegister;
    private ShapeList range;
    private boolean renderXRays;
    
    protected void registerAll(boolean b){
        Registries.CLIENT_CHUNK_LIGHT_LOAD.register(this, b);
        Registries.CLIENT_CHUNK_LOAD.register(this, b);
        Registries.BETWEEN_RENDER_FRAMES.register(this, b);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
        Registries.AFTER_CLIENT_LEVEL_CHANGE.register(this, b);
        GenericRegistry.SPAWN_CONDITION_CHANGED.register(this, b);
    }
    void reshapesAsync(){
        LongOpenHashSet packedChunkPoses = new LongOpenHashSet(canSpawnPoses.keySet());
        runningTasks.forEach(task -> packedChunkPoses.remove(task.packedChunkPos));
        delayedTasks.forEach(task -> packedChunkPoses.remove(task.packedChunkPos));
        packedChunkPoses.forEach(pos ->{
            ArrayList<BlockPos> blockPoses = new ArrayList<>(canSpawnPoses.get(pos).keySet());
            delayedTasks.add(new DelayedTask(pos, ()->new RunningTask(pos, CompletableFuture.completedFuture(new TaskResult(pos, blockPoses)))));
        });
    }
    void setRenderXRays(boolean xrays){
        shapeRegister = method.getShapeRegister(renderXRays = xrays);
        reshapesAsync();
    }
    void updateRenderXRays(){ setRenderXRays(CanSpawnDisplay.renderXRays.getAsBoolean()); }
    void setRenderMethod(IRenderMethod method){
        this.method = method;
        shapeRegister = method.getShapeRegister(renderXRays);
        reshapesAsync();
    }
    void updateRenderMethod(){ setRenderMethod(CanSpawnDisplay.renderMethod.get()); }
    void setRenderColor(int color){
        this.renderColor = color;
        reshapesAsync();
    }
    void updateRenderColor(){ setRenderColor(CanSpawnDisplay.displayColor.getIntegerValue()); }
    void setRenderRange(ShapeList range) {
        this.range = range;
        reshapesAsync();
    }
    void updateRenderRange(){ setRenderRange(CanSpawnDisplay.rangeLimit.buildShapeList()); }
    
    DataInstance(@NotNull Minecraft client){
        this.client = client;
        registerAll(true);
        updateRenderMethod();
        updateRenderColor();
        updateRenderRange();
        updateRenderXRays();
        if(client.level == null || client.player == null) return;
        Vec3 playerPos = client.player.position();
        resetData(client.level, playerPos);
    }
    
    public void clearData(){
        AlgorithmUtils.cancelTasks(runningTasks, v->v.task);
        delayedTasks.clear();
        canSpawnPoses.values().forEach(c->c.values().forEach(QuietAutoCloseable::closeIfNotNull));
        canSpawnPoses.clear();
    }
    private void addDelayedTask(Level world, ChunkPos chunkPos, LevelLightEngine light){
        Combined3x3Chunk chunks = Combined3x3Chunk.createCentered(world, chunkPos.x(), chunkPos.z());
        if(chunks == null) return;
        long packedChunkPos = chunkPos.pack();
        delayedTasks.add(new DelayedTask(packedChunkPos, ()->testChunkAsync(chunks, light, packedChunkPos)));
    }
    public void resetData(@NotNull Level world, @NotNull Vec3 playerPos){
        for(ChunkAccess chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, playerPos))
            addDelayedTask(world, chunk.getPos(), world.getLightEngine());
    }
    @Override public void onClientWorldChunkLightUpdated(@NotNull ClientLevel level, @NotNull LevelChunk chunk) {
        addDelayedTask(level, chunk.getPos(), level.getLightEngine());
    }
    @Override public void onChunkLoad(@NonNull ClientLevel level, @NonNull LevelChunk chunk) {
        ChunkPos centerChunkPos = chunk.getPos();
        for(int dx = -1; dx <= 1; ++dx)
            for(int dz = -1; dz <= 1; ++dz)
                addDelayedTask(level, new ChunkPos(centerChunkPos.x() + dx, centerChunkPos.z() + dz), level.getLightEngine());
    }
    @Override public void close() {
        clearData();
        registerAll(false);
    }
    
    void clearDataOutOfRange(double chunkedX, double chunkedZ, double radius){
        double radiusSquared = radius * radius;
        // 先清理超出范围的delayedTask
        AlgorithmUtils.fastRemove(delayedTasks, task->{
            double squaredDistance = LPCMathHelper.squaredLength(
                Packed.ChunkPos.unpackX(task.packedChunkPos) - chunkedX
                , Packed.ChunkPos.unpackZ(task.packedChunkPos) - chunkedZ);
            return squaredDistance > radiusSquared;
        });
        
        clearMapDataOutOfRange(chunkedX, chunkedZ, radiusSquared, canSpawnPoses, HashMap::isEmpty, data->data.values().forEach(QuietAutoCloseable::closeIfNotNull));
        
        runningTasks.sort(Comparator.comparingDouble(task->(
            Mth.square(Packed.ChunkPos.unpackX(task.packedChunkPos) - chunkedX)
                + Mth.square(Packed.ChunkPos.unpackZ(task.packedChunkPos) - chunkedZ)
        )));
    }
    
    @Override public void betweenFrames() {
        updateCounter = GenericConfigs.updateLimitPerFrame.getAsInt() + Math.min(updateCounter, 0);
        
        DataUtils.executeWithRenderCenterPos(this::clearDataOutOfRange, Minecraft.getInstance().options.renderDistance().get() * 2);
        
        LongOpenHashSet completedTasks = null;
        if(updateCounter <= 0) return;
        for(var task : runningTasks) {
            if(task.task.isDone()){
                var res = task.task.join();
                long packedChunkPos = res.packedChunkPos;
                var shapes = canSpawnPoses.computeIfAbsent(packedChunkPos, k->new HashMap<>());
                updateCounter -= shapes.size();
                shapes.values().forEach(QuietAutoCloseable::closeIfNotNull);
                shapes.clear();
                for(var pos : res.result) shapes.put(pos, range.testPos(pos) ? shapeRegister.registerShape(pos, renderColor) : null);
                updateCounter -= shapes.size();
                if(completedTasks == null) completedTasks = new LongOpenHashSet();
                completedTasks.add(packedChunkPos);
            }
            if(updateCounter <= 0) break;
        }
        if(completedTasks != null){
            LongOpenHashSet finalCompletedTasks = completedTasks;
            AlgorithmUtils.fastRemove(runningTasks, task->finalCompletedTasks.contains(task.packedChunkPos));
        }
        
        DataUtils.executeWithCameraCenterPos(this::scheduleDelayedTasksToRunningTasks);
        
        // if(updateCounter <= 0) return;
    }
    
    void scheduleDelayedTasksToRunningTasks(double chunkedX, double chunkedZ) {
        if(runningTasks.size() < runningTasksLimit){
            delayedTasks.sort(Comparator.comparingDouble(task->-(
                Mth.square(Packed.ChunkPos.unpackX(task.packedChunkPos) - chunkedX)
                    + Mth.square(Packed.ChunkPos.unpackZ(task.packedChunkPos) - chunkedZ)
            )));
            while (runningTasks.size() < runningTasksLimit && !delayedTasks.isEmpty())
                runningTasks.add(delayedTasks.removeLast().task.get());
        }
    }
    
    @Override public void onSpawnConditionChanged() {
        if(client.level != null && client.player != null)
            resetData(client.level, client.player.position());
    }
    @Override public void afterLevelChange(@NonNull Minecraft minecraftClient, @NonNull ClientLevel clientWorld) {clearData();}
    
    private void tryPutDelayed(Level world, int x, int z){
        Combined3x3Chunk chunk = Combined3x3Chunk.createCentered(world, x, z);
        if(chunk == null) return;
        long packedChunkPos = Packed.ChunkPos.pack(x, z);
        AlgorithmUtils.fastRemove(runningTasks, task->{
            boolean res = task.packedChunkPos == packedChunkPos;
            if(res) task.task.cancel(false);
            return res;
        });
        AlgorithmUtils.fastRemove(delayedTasks, task->task.packedChunkPos == packedChunkPos);
        var light = world.getLightEngine();
        delayedTasks.add(new DelayedTask(packedChunkPos, ()->testChunkAsync(chunk, light, packedChunkPos)));
    }
    @Override public void onClientWorldChunkSetBlockState(LevelChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState) {
        if(newState != null && newState.getBlock() == Blocks.WATER) return;
        int x = pos.getX() >> 4, z = pos.getZ() >> 4;
        Level world = chunk.getLevel();
        for(int dx = -1; dx <= 1; ++dx)
            for(int dz = -1; dz <= 1; ++dz)
                tryPutDelayed(world, x + dx, z + dz);
    }
    
    private RunningTask testChunkAsync(@NotNull Combined3x3Chunk chunk, @NotNull LevelLightEngine lightingProvider, long packedChunkPos){
        return new RunningTask(packedChunkPos, GenericUtils.supplyAsync(()->AsyncChunkTest(chunk, lightingProvider, packedChunkPos)));
    }
    
    private TaskResult AsyncChunkTest(@NotNull Combined3x3Chunk chunk, @NotNull LevelLightEngine light, long packedChunkPos){
        TaskResult result = new TaskResult(packedChunkPos, new ArrayList<>());
        int x = Packed.getBlockCoord(Packed.ChunkPos.unpackX(packedChunkPos));
        int z = Packed.getBlockCoord(Packed.ChunkPos.unpackZ(packedChunkPos));
        GenericUtils.MobSpawnTest spawnTest = GenericUtils.createSpawnTest();
        Iterable<BlockPos> blockPoses = AlgorithmUtils.iterateInBox(
            x, chunk.getMinY(), z, x + 15, chunk.getMinY() + chunk.getHeight() - 1, z + 15);
        for(BlockPos pos1 : blockPoses) {
            if(spawnTest.mayMobSpawnAt(chunk, light, pos1))
                result.result.add(pos1.immutable());
        }
        return result;
    }
    
    private static class Combined3x3Chunk extends CombinedBlockGetters {
        private Combined3x3Chunk(){}
        static @Nullable Combined3x3Chunk createCentered(Level level, int x, int z){
            ArrayList<ChunkAccess> chunks = new ArrayList<>(9);
            for(int dx = -1; dx <= 1; ++dx) {
                for(int dz = -1; dz <= 1; ++dz) {
                    ChunkAccess chunk1 = level.getChunk(x + dx, z + dz, ChunkStatus.FULL, false);
                    if(chunk1 == null) return null;
                    else chunks.add(chunk1);
                }
            }
            Combined3x3Chunk res = new Combined3x3Chunk();
            chunks.forEach(res::putChunk);
            return res;
        }
    }
}
