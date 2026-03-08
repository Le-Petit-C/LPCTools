package lpctools.tools.canSpawnDisplay;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericConfigs;
import lpctools.generic.GenericRegistry;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeReference;
import lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance;
import lpctools.util.AlgorithmUtils;
import lpctools.util.LPCMathHelper;
import lpctools.util.Packed;
import lpctools.util.javaex.QuietAutoCloseable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static lpctools.tools.ToolUtils.clearMapDataOutOfRange;

public class DataInstance implements AutoCloseable, Registries.ClientWorldChunkLightUpdated, Registries.WorldLastRender, Registries.ClientWorldChunkSetBlockState, GenericRegistry.SpawnConditionChanged, ClientWorldEvents.AfterClientWorldChange {
    private record DelayedTask(long packedChunkPos, Supplier<RunningTask> task){}
    private record RunningTask(long packedChunkPos, CompletableFuture<TaskResult> task){}
    private record TaskResult(long packedChunkPos, ArrayList<BlockPos> result){}
    
    public final Long2ObjectOpenHashMap<HashMap<BlockPos, ShapeReference>> canSpawnPoses = new Long2ObjectOpenHashMap<>();
    public final @NotNull MinecraftClient client;
    
    private final ArrayList<RunningTask> runningTasks = new ArrayList<>();
    private final ArrayList<DelayedTask> delayedTasks = new ArrayList<>();
    private final int runningTasksLimit = Runtime.getRuntime().availableProcessors();
    
    private int updateCounter = 0;
    private int renderColor;
    private IRenderMethod method;
    private RenderInstance renderInstance;
    private ShapeList range;
    private boolean renderXRays;
    
    protected void registerAll(boolean b){
        Registries.CLIENT_CHUNK_LIGHT_LOAD.register(this, b);
        Registries.MASA_WORLD_RENDER_LAST.register(this, b);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
        Registries.AFTER_CLIENT_WORLD_CHANGE.register(this, b);
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
        renderInstance = method.getRenderInstance(renderXRays = xrays);
        reshapesAsync();
    }
    void updateRenderXRays(){ setRenderXRays(CanSpawnDisplay.renderXRays.getAsBoolean()); }
    void setRenderMethod(IRenderMethod method){
        this.method = method;
        renderInstance = method.getRenderInstance(renderXRays);
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
    
    DataInstance(@NotNull MinecraftClient client){
        this.client = client;
        registerAll(true);
        updateRenderMethod();
        updateRenderColor();
        updateRenderRange();
        updateRenderXRays();
        if(client.world == null || client.player == null) return;
        Vec3d playerPos = client.player.getEntityPos();
        resetData(client.world, playerPos);
    }
    
    public void clearData(){
        AlgorithmUtils.cancelTasks(runningTasks, v->v.task);
        delayedTasks.clear();
        canSpawnPoses.values().forEach(c->c.values().forEach(QuietAutoCloseable::closeIfNotNull));
        canSpawnPoses.clear();
    }
    private void addDelayedTask(Chunk chunk, LightingProvider light){
        long packedChunkPos = chunk.getPos().toLong();
        delayedTasks.add(new DelayedTask(packedChunkPos, ()->testChunkAsync(chunk, light, packedChunkPos)));
    }
    public void resetData(@NotNull World world, @NotNull Vec3d playerPos){
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, playerPos))
            addDelayedTask(chunk, world.getLightingProvider());
    }
    @Override public void onClientWorldChunkLightUpdated(@NotNull ClientWorld world, @NotNull WorldChunk chunk) {
        addDelayedTask(chunk, world.getLightingProvider());
    }
    @Override public void close() {
        clearData();
        registerAll(false);
    }
    @Override public void onLast(Registries.MASAWorldRenderContext context) {
        updateCounter = GenericConfigs.updateLimitPerFrame.getAsInt() + Math.min(updateCounter, 0);
        
        double squaredDistanceLimit = MathHelper.square((double)MinecraftClient.getInstance().options.getViewDistance().getValue() * 2);
        var camPos = context.camera().getCameraPos();
        double chunkedCamX = camPos.x / 16 - 0.5, chunkedCamZ = camPos.z / 16 - 0.5;
        
        // 先清理超出范围的delayedTask
        AlgorithmUtils.fastRemove(delayedTasks, task->{
            double squaredDistance = LPCMathHelper.squaredLength(
                Packed.ChunkPos.unpackX(task.packedChunkPos) - chunkedCamX
                , Packed.ChunkPos.unpackZ(task.packedChunkPos) - chunkedCamZ);
            return squaredDistance > squaredDistanceLimit;
        });
        
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, squaredDistanceLimit, canSpawnPoses, HashMap::isEmpty, data->data.values().forEach(QuietAutoCloseable::closeIfNotNull));
        
        runningTasks.sort(Comparator.comparingDouble(task->(
            MathHelper.square(Packed.ChunkPos.unpackX(task.packedChunkPos) - chunkedCamX)
                + MathHelper.square(Packed.ChunkPos.unpackZ(task.packedChunkPos) - chunkedCamZ)
        )));
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
                for(var pos : res.result) shapes.put(pos, range.testPos(pos) ? renderInstance.addShape(method.getShape(pos, renderColor, renderXRays)) : null);
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
        
        if(runningTasks.size() < runningTasksLimit){
            delayedTasks.sort(Comparator.comparingDouble(task->-(
                MathHelper.square(Packed.ChunkPos.unpackX(task.packedChunkPos) - chunkedCamX)
                + MathHelper.square(Packed.ChunkPos.unpackZ(task.packedChunkPos) - chunkedCamZ)
            )));
            while (runningTasks.size() < runningTasksLimit && !delayedTasks.isEmpty())
                runningTasks.add(delayedTasks.removeLast().task.get());
        }
        // if(updateCounter <= 0) return;
    }
    
    @Override public void onSpawnConditionChanged() {
        if(client.world != null && client.player != null)
            resetData(client.world, client.player.getEntityPos());
    }
    @Override public void afterWorldChange(MinecraftClient minecraftClient, ClientWorld clientWorld) {clearData();}
    
    private void tryPutDelayed(World world, int x, int z){
        for(int dz = -1; dz <= 1; ++dz)
            for(int dx = -1; dx <= 1; ++dx)
                if(!world.isChunkLoaded(x + dx, z + dz)) return;
        long packedChunkPos = Packed.ChunkPos.pack(x, z);
        Chunk chunk = world.getChunk(x, z, ChunkStatus.FULL, false);
        if(chunk == null) return;
        AlgorithmUtils.fastRemove(runningTasks, task->{
            boolean res = task.packedChunkPos == packedChunkPos;
            if(res) task.task.cancel(false);
            return res;
        });
        AlgorithmUtils.fastRemove(delayedTasks, task->task.packedChunkPos == packedChunkPos);
        var light = world.getLightingProvider();
        delayedTasks.add(new DelayedTask(packedChunkPos, ()->testChunkAsync(chunk, light, packedChunkPos)));
    }
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState) {
        if(newState != null && newState.getBlock() == Blocks.WATER) return;
        int x = pos.getX() >> 4, z = pos.getZ() >> 4;
        World world = chunk.getWorld();
        for(int dx = -1; dx <= 1; ++dx)
            for(int dz = -1; dz <= 1; ++dz)
                tryPutDelayed(world, x + dx, z + dz);
    }
    
    private RunningTask testChunkAsync(@NotNull Chunk chunk, @NotNull LightingProvider lightingProvider, long packedChunkPos){
        return new RunningTask(packedChunkPos, GenericUtils.supplyAsync(()->AsyncChunkTest(chunk, lightingProvider, packedChunkPos)));
    }
    
    private TaskResult AsyncChunkTest(@NotNull Chunk chunk, @NotNull LightingProvider light, long packedChunkPos){
        TaskResult result = new TaskResult(packedChunkPos, new ArrayList<>());
        int x = Packed.getBlockCoord(Packed.ChunkPos.unpackX(packedChunkPos));
        int z = Packed.getBlockCoord(Packed.ChunkPos.unpackZ(packedChunkPos));
        Iterable<BlockPos> blockPoses = AlgorithmUtils.iterateInBox(
            x, chunk.getBottomY(), z,
            x + 15, chunk.getBottomY() + chunk.getHeight() - 1, z + 15);
        for(BlockPos pos1 : blockPoses){
            if(GenericUtils.mayMobSpawnAt(chunk, light, pos1))
                result.result.add(pos1.toImmutable());
        }
        return result;
    }
}
