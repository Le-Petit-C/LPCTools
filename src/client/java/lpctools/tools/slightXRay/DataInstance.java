package lpctools.tools.slightXRay;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.translucentShapes.Quad;
import lpctools.lpcfymasaapi.render.translucentShapes.RenderInstance;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeReference;
import lpctools.util.AlgorithmUtils;
import lpctools.util.LPCMathHelper;
import lpctools.util.Packed;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static lpctools.generic.GenericConfigs.updateLimitPerFrame;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.slightXRay.SlightXRayData.*;
import static lpctools.util.AlgorithmUtils.iterateInManhattanDistance;
import static lpctools.util.BlockUtils.isFluid;

class DataInstance implements AutoCloseable, ClientChunkEvents.Load, ClientWorldEvents.AfterClientWorldChange, Registries.ClientWorldChunkSetBlockState, Registries.WorldLastRender {
    private static final RenderInstance renderInstance = RenderInstance.shapeInstanceDepthless();
    
    private interface TaskGenerator { RunningTask generate(ShapeList shapeList); }
    
    private record DelayedTask(long packedChunkPos, TaskGenerator taskGenerator){}
    private record RunningTask(long packedChunkPos, CompletableFuture<TaskResult> future){}
    private record TaskResult(long packedChunkPos, @NotNull Int2ObjectOpenHashMap<MutableInt> markedPoses, IntOpenHashSet shownPoses, IntOpenHashSet posesNeedToUpdate){}
    
    private final Long2ObjectOpenHashMap<RunningTask> runningTasks = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<DelayedTask> delayedTasks = new Long2ObjectOpenHashMap<>();
    // 为方便清理操作，给markedPoses分块，区块坐标->区块local坐标->颜色
    private final Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<MutableInt>> markedPoses = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<IntOpenHashSet> renderingPoses = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<ShapeReference[]>> posQuads = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<IntOpenHashSet> posesNeedToUpdateRender = new Long2ObjectOpenHashMap<>();
	private final int runningTasksLimit = Runtime.getRuntime().availableProcessors();
    
    private int updateCounter = 0;
    private boolean useCullFace;
    private ShapeList shapeList;
    
    void reshapesAsync() {
        LongOpenHashSet dataChunks = new LongOpenHashSet(markedPoses.keySet());
        dataChunks.removeAll(delayedTasks.keySet());
        for(long packedChunkPos : dataChunks) {
            delayedTasks.put(packedChunkPos, new DelayedTask(
                packedChunkPos, rangeLimit->{
                    var recordedPosQuadKeys = recordCollection(new IntOpenHashSet(), posQuads.get(packedChunkPos), Int2ObjectOpenHashMap::keySet);
                    var recordedMarkedPoses = recordMap(new Int2ObjectOpenHashMap<>(), markedPoses.get(packedChunkPos));
                    updateCounter -= (recordedPosQuadKeys.size() + recordedMarkedPoses.size()) / 16;
					return new RunningTask(packedChunkPos,
						GenericUtils.supplyAsync(()->{
							var taskResult = new TaskResult(packedChunkPos, recordedMarkedPoses, new IntOpenHashSet(), recordedPosQuadKeys);
							buildShownPoses(taskResult.shownPoses, rangeLimit, taskResult.markedPoses.keySet(), packedChunkPos);
							markEdgePoses(taskResult.shownPoses, taskResult.shownPoses, taskResult.posesNeedToUpdate);
							return taskResult;
						}
					));
                }
            ));
        }
    }
    
    // 更新某pos的渲染面状态
    private void updatePosRender(long packedBlockPos){
        --updateCounter;
        int x = BlockPos.unpackLongX(packedBlockPos);
        int y = BlockPos.unpackLongY(packedBlockPos);
        int z = BlockPos.unpackLongZ(packedBlockPos);
        ShapeReference[] old = chunkedRemoveKey(posQuads, x, y, z);
        if(!chunkedContains(renderingPoses, x, y, z)) {
            if(old != null) {
                for(var ref : old)
                    if(ref != null) ref.close();
            }
            return;
        }
        var colorSource = chunkedGet(markedPoses, packedBlockPos);
        
        int color = colorSource.intValue();
        var quads = old == null ? new ShapeReference[Direction.values().length] : old;
        for(int i = 0; i < quads.length; ++i){
            if(quads[i] != null) {
                quads[i].close();
                quads[i] = null;
            }
        }
        class Temp{
            static final int[][] quadOffsets = new int[Direction.values().length][];
            static {
                quadOffsets[Direction.WEST .getIndex()] = new int[]{Direction.WEST .getIndex(),-1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0};
                quadOffsets[Direction.EAST .getIndex()] = new int[]{Direction.EAST .getIndex(), 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1};
                quadOffsets[Direction.DOWN .getIndex()] = new int[]{Direction.DOWN .getIndex(), 0,-1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1};
                quadOffsets[Direction.UP   .getIndex()] = new int[]{Direction.UP   .getIndex(), 0, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0};
                quadOffsets[Direction.NORTH.getIndex()] = new int[]{Direction.NORTH.getIndex(), 0, 0,-1, 0, 0, 0, 0, 1, 0, 1, 0, 0};
                quadOffsets[Direction.SOUTH.getIndex()] = new int[]{Direction.SOUTH.getIndex(), 0, 0, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0};
            }
        }
        for(var o : Temp.quadOffsets){
            if(!chunkedContains(renderingPoses, x + o[1], y + o[2], z + o[3]))
                quads[o[0]] = renderInstance.addShape(new Quad(x + o[4], y + o[5], z + o[6], o[7], o[8], o[9], o[10], o[11], o[12], color, useCullFace));
            else {
                Direction attachedDirection = Direction.byIndex(o[0]);
                Direction oppositeDirection = attachedDirection.getOpposite();
                int oppositeIndex = oppositeDirection.getIndex();
                long attachedBlockPos = BlockPos.offset(packedBlockPos, attachedDirection);
                var attached = chunkedGet(posQuads, attachedBlockPos);
                if(attached != null && attached[oppositeIndex] != null) {
                    attached[oppositeIndex].close();
                    attached[oppositeIndex] = null;
                    boolean isEmpty = true;
                    for(var v : attached) {
                        if(v != null) {
                            isEmpty = false;
                            break;
                        }
                    }
                    if(isEmpty) chunkedRemoveKey(posQuads, attachedBlockPos);
                }
            }
        }
		for (ShapeReference quad : quads) {
			if (quad != null) {
				chunkedPut(posQuads, x, y, z, quads);
				break;
			}
		}
    }
    
    void clearQuads(Iterable<ShapeReference[]> quadSet) {
        for(var quads : quadSet){
            for(var quad : quads) {
                if (quad != null) {
                    quad.close();
                    --updateCounter;
                }
            }
        }
    }
    
    // 清理超出距离的区块
    public void clearChunksOutOfRange(double chunkedCamX, double chunkedCamZ) {
        double distanceLimitSquared = MathHelper.square(2.0 * MinecraftClient.getInstance().options.getViewDistance().getValue());
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, distanceLimitSquared, delayedTasks, null, null);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, distanceLimitSquared, runningTasks, null, task->task.future.cancel(true));
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, distanceLimitSquared, markedPoses, Int2ObjectOpenHashMap::isEmpty, null);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, distanceLimitSquared, renderingPoses, IntOpenHashSet::isEmpty, null);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, distanceLimitSquared, posesNeedToUpdateRender, IntOpenHashSet::isEmpty, null);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, distanceLimitSquared, posQuads, Int2ObjectOpenHashMap::isEmpty, quads->clearQuads(quads.values()));
    }
    
    void updatePosesNeedToUpdate(double chunkedCamX, double chunkedCamZ) {
        if(updateCounter <= 0) return;
        if(!posesNeedToUpdateRender.isEmpty()) {
            LongHeapPriorityQueue chunksNeedToUpdate = new LongHeapPriorityQueue(posesNeedToUpdateRender.keySet(),
                LongComparator.comparingDouble(packedChunkPos->{
                int x = Packed.ChunkPos.unpackX(packedChunkPos);
                int z = Packed.ChunkPos.unpackZ(packedChunkPos);
                return MathHelper.square(x - chunkedCamX) + MathHelper.square(z - chunkedCamZ);
            }));
            while(!chunksNeedToUpdate.isEmpty()){
                long packedChunkPos = chunksNeedToUpdate.firstLong();
                var chunkUpdates = posesNeedToUpdateRender.get(packedChunkPos);
                var it = chunkUpdates.iterator();
                while(it.hasNext()){
                    updatePosRender(Packed.BlockPos.packedFromChunkLocal(packedChunkPos, it.nextInt()));
                    it.remove();
                    if(--updateCounter <= 0) return;
                }
                posesNeedToUpdateRender.remove(chunksNeedToUpdate.dequeueLong());
            }
        }
    }
    
    static LongHeapPriorityQueue nearFirstPackedChunkPos(LongCollection packedChunkPoses, double chunkedCamX, double chunkedCamZ) {
        return new LongHeapPriorityQueue(packedChunkPoses,
            LongComparator.comparingDouble(packedChunkPos -> LPCMathHelper.squaredLength(
                Packed.ChunkPos.unpackX(packedChunkPos) - chunkedCamX,
                Packed.ChunkPos.unpackZ(packedChunkPos) - chunkedCamZ
            ))
        );
    }
    
    void consumeTasks(double chunkedCamX, double chunkedCamZ){
        LongArrayList completedFutures = null;
        if(!runningTasks.isEmpty()) {
            var runningTaskKeys = nearFirstPackedChunkPos(runningTasks.keySet(), chunkedCamX, chunkedCamZ);
            while(!runningTaskKeys.isEmpty()){
                var data = runningTasks.get(runningTaskKeys.dequeueLong());
                if(!data.future.isDone()) continue;
                var result = data.future.join();
                markedPoses.put(result.packedChunkPos, result.markedPoses);
                renderingPoses.put(result.packedChunkPos, result.shownPoses);
                posesNeedToUpdateRender.put(result.packedChunkPos,
                    combineCollections(result.posesNeedToUpdate, posesNeedToUpdateRender.get(result.packedChunkPos), size->updateCounter -= size / 16));
                
                if(updateCounter <= 0) break;
                if(completedFutures == null) completedFutures = new LongArrayList();
                completedFutures.add(result.packedChunkPos);
            }
            if(completedFutures != null && !completedFutures.isEmpty()) {
                completedFutures.forEach(runningTasks::remove);
                completedFutures.clear();
            }
        }
        if(!delayedTasks.isEmpty() && runningTasks.size() < runningTasksLimit){
            var delayedTaskKeys = nearFirstPackedChunkPos(delayedTasks.keySet(), chunkedCamX, chunkedCamZ);
            while(!delayedTaskKeys.isEmpty() && runningTasks.size() < runningTasksLimit) {
                long key = delayedTaskKeys.dequeueLong();
                if(runningTasks.containsKey(key)) continue;
                var delayedTask = delayedTasks.remove(key);
                runningTasks.put(delayedTask.packedChunkPos, delayedTask.taskGenerator.generate(shapeList));
            }
        }
    }
    
    @Override public void onLast(Registries.MASAWorldRenderContext context) {
        updateCounter = updateLimitPerFrame.getAsInt() + Math.min(updateCounter, 0);
        SlightXRay.tryRefreshXRayBlocks();
        var camPos = context.camera().getPos();
        double chunkedCamX = camPos.x / 16 - 0.5, chunkedCamZ = camPos.z / 16 - 0.5;
        clearChunksOutOfRange(chunkedCamX, chunkedCamZ);
        if(updateCounter <= 0) return;
        consumeTasks(chunkedCamX, chunkedCamZ);
        if(updateCounter <= 0) return;
        updatePosesNeedToUpdate(chunkedCamX, chunkedCamZ);
    }
    
    void registerAll(boolean b){
        Registries.AFTER_CLIENT_WORLD_CHANGE.register(this, b);
        Registries.MASA_WORLD_RENDER_LAST.register(this, b);
        Registries.CLIENT_CHUNK_LOAD.register(this, b);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
    }
    
    void setUseCullFace(boolean useCullFace) {
        this.useCullFace = useCullFace;
        reshapesAsync();
    }
    void updateUseCullFace() { setUseCullFace(SlightXRay.useCullFace.getAsBoolean()); }
    
    void setRangeLimit(ShapeList shapeList) {
        this.shapeList = shapeList;
        reshapesAsync();
    }
    void updateRangeLimit() { setRangeLimit(SlightXRay.displayRange.buildShapeList()); }
    
    DataInstance() {
        updateUseCullFace();
        updateRangeLimit();
        registerAll(true);
        resetData();
    }
    
    public void resetData(){
        var client = MinecraftClient.getInstance();
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if(player == null || world == null) return;
        Vec3d playerEyePos = player.getEyePos();
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, playerEyePos)){
            ChunkPos chunkPos = chunk.getPos();
            testChunkAsync(chunkPos.x, chunkPos.z, world);
        }
    }
    @Override public void close(){
        registerAll(false);
        clearData();
    }
    @Override public void onChunkLoad(ClientWorld world, WorldChunk chunk) {
        ChunkPos pos = chunk.getPos();
        testChunkAsync(pos.x, pos.z, world);
        testChunkAsync(pos.x - 1, pos.z, world);
        testChunkAsync(pos.x + 1, pos.z, world);
        testChunkAsync(pos.x, pos.z - 1, world);
        testChunkAsync(pos.x, pos.z + 1, world);
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
    
    protected void clearData(){
        runningTasks.values().forEach(v->v.future.cancel(true));
        runningTasks.clear();
        delayedTasks.clear();
        for(var quadSet : posQuads.values())
            for(var quads : quadSet.values())
                for(var ref : quads)
                    if(ref != null) ref.close();
        posQuads.clear();
        posesNeedToUpdateRender.clear();
        markedPoses.clear();
    }
    
    // 检测pos处是否需要进行标记并更新标记
    private void testPos(World world, BlockPos pos){
        long packedBlockPos = pos.asLong();
        BlockState state = world.getBlockState(pos);
        MutableInt color;
        synchronized (XRayBlocks){
            color = XRayBlocks.getOrDefault(state.getBlock(), null);
        }
        
        MutableInt res = null;
        if(color != null) {
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2)) {
                if(doShowAround(world.getBlockState(pos1))){
                    res = color;
                    break;
                }
            }
        }
        boolean oldChanged;
        if(res == null) {
            oldChanged = chunkedRemove(renderingPoses, packedBlockPos);
            chunkedRemoveKey(markedPoses, packedBlockPos);
        }
        else {
            oldChanged = shapeList.testPos(packedBlockPos) && chunkedAdd(renderingPoses, packedBlockPos);
            chunkedPut(markedPoses, packedBlockPos, res);
        }
		if(oldChanged || res != null) chunkedAdd(posesNeedToUpdateRender, packedBlockPos);
    }
    
    private static boolean doShowAround(BlockState state){ return !state.isOpaque() || state.isTransparent(); }
    
    private RunningTask buildRunningTask(ChunkData chunkData, ShapeList rangeLimit, long packedChunkPos){
        Int2ObjectOpenHashMap<MutableInt> recordedOldMarkedPoses = recordMap(new Int2ObjectOpenHashMap<>(), markedPoses.get(packedChunkPos));
        IntOpenHashSet recordedOldShownPoses = recordCollection(new IntOpenHashSet(), renderingPoses.get(packedChunkPos));
        HashMap<Block, MutableInt> copy; synchronized (XRayBlocks){ copy = new HashMap<>(XRayBlocks); }
        updateCounter -= (recordedOldMarkedPoses.size() + recordedOldShownPoses.size()) / 16;
        return new RunningTask(packedChunkPos, GenericUtils.supplyAsync(()->testChunkData(chunkData, copy, rangeLimit, recordedOldMarkedPoses, recordedOldShownPoses)));
    }
    
    private void testChunkAsync(int chunkX, int chunkZ, ClientWorld world){
        long packedChunkPos = Packed.ChunkPos.pack(chunkX, chunkZ);
        ChunkData task = ChunkData.buildData(chunkX, chunkZ, world);
        if(task != null) delayedTasks.put(packedChunkPos, new DelayedTask(packedChunkPos, rangeLimit->buildRunningTask(task, rangeLimit, packedChunkPos)));
    }
    private static void buildShownPoses(IntOpenHashSet shownPoses, ShapeList rangeLimit, IntSet chunkLocalMarkedPoses, long packedChunkPos) {
        int chunkBlockX = Packed.getBlockCoord(Packed.ChunkPos.unpackX(packedChunkPos));
        int chunkBlockZ = Packed.getBlockCoord(Packed.ChunkPos.unpackZ(packedChunkPos));
        var it = chunkLocalMarkedPoses.iterator();
        while (it.hasNext()) {
            int localPos = it.nextInt();
            int x = chunkBlockX + Packed.ChunkLocal.unpackX(localPos);
            int y = Packed.ChunkLocal.unpackY(localPos);
            int z = chunkBlockZ + Packed.ChunkLocal.unpackZ(localPos);
            if(rangeLimit.testPos(x, y, z))
                shownPoses.add(localPos);
        }
    }
    static void markEdgePoses(IntSet poses, IntIterable posesToTest, IntOpenHashSet markedPoses) {
        var it = posesToTest.iterator();
        while(it.hasNext()){
            var pos = it.nextInt();
            int x = Packed.ChunkLocal.unpackX(pos);
            int y = Packed.ChunkLocal.unpackY(pos);
            int z = Packed.ChunkLocal.unpackZ(pos);
            for(var direction : Direction.values()){
                if(!poses.contains(Packed.ChunkLocal.pack(x + direction.getOffsetX(), y + direction.getOffsetY(), z + direction.getOffsetZ()))){
                    markedPoses.add(pos);
                    break;
                }
            }
        }
    }
    private TaskResult testChunkData(ChunkData data, HashMap<Block, MutableInt> colorMap, ShapeList rangeLimit
        , Int2ObjectOpenHashMap<MutableInt> recordedOldMarkedPoses, IntOpenHashSet recordedOldShownPoses){
        long packedChunkPos = data.current.getPos().toLong();
        TaskResult res = new TaskResult(packedChunkPos, recordedOldMarkedPoses, new IntOpenHashSet(), new IntOpenHashSet());
        markEdgePoses(recordedOldShownPoses, recordedOldShownPoses, res.posesNeedToUpdate);
        recordedOldShownPoses.clear();
        IntArrayList newMarkedPoses = new IntArrayList();
        int bottom = data.current.getBottomY(), height = data.current.getHeight(), top = bottom + height;
        ChunkTestData displaysNear = new ChunkTestData(bottom, height);
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, 0, 15, top - 1, 15))
            displaysNear.set(pos1, doShowAround(data.current.getBlockState(pos1)));
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(-1, bottom, 0, -1, top - 1, 15))
            displaysNear.set(pos1, doShowAround(data.west.getBlockState(pos1)));
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(16, bottom, 0, 16, top - 1, 15))
            displaysNear.set(pos1, doShowAround(data.east.getBlockState(pos1)));
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, -1, 15, top - 1, -1))
            displaysNear.set(pos1, doShowAround(data.north.getBlockState(pos1)));
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, 16, 15, top - 1, 16))
            displaysNear.set(pos1, doShowAround(data.south.getBlockState(pos1)));
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom - 1, 0, 15, bottom - 1, 15))
            displaysNear.set(pos1, true);
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, top, 0, 15, top, 15))
            displaysNear.set(pos1, true);
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, 0, 15, top - 1, 15)){
            BlockState state = data.current.getBlockState(pos1);
            MutableInt color = colorMap.get(state.getBlock());
            if(color == null) continue;
            if(displaysNear.get(pos1)){
                int chunkLocalPos = Packed.ChunkLocal.pack(pos1);
                if(!res.markedPoses.containsKey(chunkLocalPos)) newMarkedPoses.add(chunkLocalPos);
                res.markedPoses.put(chunkLocalPos, color);
                continue;
            }
            for(Direction direction : Direction.values()){
                if(displaysNear.get(pos1.getX() + direction.getOffsetX(), pos1.getY() + direction.getOffsetY(), pos1.getZ() + direction.getOffsetZ())){
                    int chunkLocalPos = Packed.ChunkLocal.pack(pos1);
                    if(!res.markedPoses.containsKey(chunkLocalPos)) newMarkedPoses.add(chunkLocalPos);
                    res.markedPoses.put(chunkLocalPos, color);
                    break;
                }
            }
        }
        buildShownPoses(res.shownPoses, rangeLimit, res.markedPoses.keySet(), packedChunkPos);
        markEdgePoses(res.shownPoses, newMarkedPoses, res.posesNeedToUpdate);
        return res;
    }
    private record ChunkData(Chunk current, Chunk west, Chunk east, Chunk north, Chunk south){
        static @Nullable DataInstance.ChunkData buildData(int chunkX, int chunkZ, ClientWorld world){
            ChunkData task = new ChunkData(
                world.getChunk(chunkX, chunkZ, ChunkStatus.FULL, false),
                world.getChunk(chunkX - 1, chunkZ, ChunkStatus.FULL, false),
                world.getChunk(chunkX + 1, chunkZ, ChunkStatus.FULL, false),
                world.getChunk(chunkX, chunkZ - 1, ChunkStatus.FULL, false),
                world.getChunk(chunkX, chunkZ + 1, ChunkStatus.FULL, false));
            if(task.current == null || task.west == null || task.east == null || task.north == null || task.south == null)
                return null;
            return task;
        }
    }
    private static class ChunkTestData {
        public final boolean[][][] data;
        public final int bottomY, worldHeight;
        ChunkTestData(int bottomY, int worldHeight){
            this.bottomY = bottomY; this.worldHeight = worldHeight;
            data = new boolean[18][worldHeight + 2][18];
        }
        boolean get(int x, int y, int z){ return data[x + 1][y - bottomY + 1][z + 1]; }
        boolean get(BlockPos pos){ return get(pos.getX(), pos.getY(), pos.getZ()); }
        void set(int x, int y, int z, boolean value){ data[x + 1][y - bottomY + 1][z + 1] = value; }
        void set(BlockPos pos, boolean value){ set(pos.getX(), pos.getY(), pos.getZ(), value); }
    }
}
