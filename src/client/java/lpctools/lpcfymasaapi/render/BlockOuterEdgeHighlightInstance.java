package lpctools.lpcfymasaapi.render;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.ChunkedTaskInstance;
import lpctools.generic.UpdateCounter;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.translucentShapes.Quad;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeReference;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeRegister;
import lpctools.util.DataUtils;
import lpctools.util.Packed;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import static lpctools.tools.ToolUtils.*;

public class BlockOuterEdgeHighlightInstance implements AutoCloseable, ClientWorldEvents.AfterClientWorldChange, Registries.BetweenRenderFrames {
    private final ChunkedTaskInstance taskInstance = new ChunkedTaskInstance(-2);
    
    // 为方便清理操作，给markedPoses分块，区块坐标->区块local坐标->颜色
    private final Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<MutableInt>> markedPoses = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<IntOpenHashSet> renderingPoses = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<ShapeReference[]>> posQuads = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<IntOpenHashSet> posesNeedToUpdateRender = new Long2ObjectOpenHashMap<>();
    
    private ShapeRegister<Quad> shapeRegister = Quad.register(true);
    
    private boolean useCullFace;
    private @Nullable ShapeList shapeList;
    
    private record TaskResult(@NotNull Int2ObjectOpenHashMap<MutableInt> markedPoses, IntOpenHashSet shownPoses, IntOpenHashSet posesNeedToUpdate){}
    
    public BlockOuterEdgeHighlightInstance() { registerAll(true); }
    
    public void setUseCullFace(boolean useCullFace) {
        this.useCullFace = useCullFace;
        reshapesAsync();
    }
    
    public void setRenderXRays(boolean xRays) {
        shapeRegister = Quad.register(xRays);
        reshapesAsync();
    }
    
    public void setRangeLimit(ShapeList shapeList) {
        this.shapeList = shapeList;
        reshapesAsync();
    }
    
    public boolean isEmpty() { return markedPoses.isEmpty() && taskInstance.isEmpty(); }
    public boolean containsKey(long packedBlockPos) { return chunkedContainsKey(markedPoses, packedBlockPos); }
    public boolean containsKey(BlockPos pos) { return chunkedContainsKey(markedPoses, pos.getX(), pos.getY(), pos.getZ()); }
    
    // 清理超出距离的区块
    public void clearChunksOutOfRange(double chunkedCamX, double chunkedCamZ, double chunkedDistanceSquared) {
        taskInstance.clearTasksOutOfRange(chunkedCamX, chunkedCamZ, chunkedDistanceSquared);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, chunkedDistanceSquared, markedPoses, Int2ObjectOpenHashMap::isEmpty, null);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, chunkedDistanceSquared, renderingPoses, IntOpenHashSet::isEmpty, null);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, chunkedDistanceSquared, posesNeedToUpdateRender, IntOpenHashSet::isEmpty, null);
        clearMapDataOutOfRange(chunkedCamX, chunkedCamZ, chunkedDistanceSquared, posQuads, Int2ObjectOpenHashMap::isEmpty, quads->clearQuads(quads.values()));
    }
    
    public void clearData(){
        taskInstance.clearTasks();
        for(var quadSet : posQuads.values())
            for(var quads : quadSet.values())
                for(var ref : quads)
                    if(ref != null) ref.close();
        posQuads.clear();
        posesNeedToUpdateRender.clear();
        markedPoses.clear();
    }
    
    public void mark(int x, int y, int z, @Nullable MutableInt color) {
        boolean oldChanged;
        if(color == null) {
            oldChanged = chunkedRemove(renderingPoses, x, y, z);
            chunkedRemoveKey(markedPoses, x, y, z);
        }
        else {
            oldChanged = (shapeList == null || shapeList.testPos(x, y, z)) && chunkedAdd(renderingPoses, x, y, z);
            chunkedPut(markedPoses, x, y, z, color);
        }
        if(oldChanged || color != null) {
            chunkedAdd(posesNeedToUpdateRender, x, y, z);
            for(var d : Direction.values())
                chunkedAdd(posesNeedToUpdateRender, x + d.getOffsetX(), y + d.getOffsetY(), z + d.getOffsetZ());
        }
    }
    
    public void mark(long packedBlockPos, @Nullable MutableInt color) {
        mark(Packed.BlockPos.unpackX(packedBlockPos), Packed.BlockPos.unpackY(packedBlockPos), Packed.BlockPos.unpackZ(packedBlockPos), color);
    }
    
    public void mark(BlockPos pos, @Nullable MutableInt color) { mark(pos.getX(), pos.getY(), pos.getZ(), color); }
    
    public void resetChunk(long packedChunkPos, Int2ObjectOpenHashMap<MutableInt> markedPoses) {
        taskInstance.scheduleTask(packedChunkPos, callback->{
            var basePosesNeedToUpdate = recordCollection(new IntOpenHashSet(), posQuads.get(packedChunkPos), Int2ObjectOpenHashMap::keySet);
            callback.task = ()->{
                TaskResult res = generateFullTaskResult(packedChunkPos, markedPoses, shapeList, basePosesNeedToUpdate);
                return ()->resetChunk(packedChunkPos, res);
            };
        });
    }
    
    @Override public void close(){
        registerAll(false);
        clearData();
        taskInstance.close();
    }
    
    @Override public void afterWorldChange(@NonNull MinecraftClient mc, @NonNull ClientWorld world) {clearData();}
    
    @Override public void betweenFrames() {
        var camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        double chunkedCamX = DataUtils.chunkedCoord(camPos.x);
        double chunkedCamZ = DataUtils.chunkedCoord(camPos.z);
        updatePosesNeedToUpdate(chunkedCamX, chunkedCamZ);
    }
    
    public void reshapesAsync() {
        LongOpenHashSet dataChunks = new LongOpenHashSet(markedPoses.keySet());
        dataChunks.removeAll(taskInstance.delayedTaskChunkPoses());
        for(long packedChunkPos : dataChunks) {
            taskInstance.scheduleTask(
                packedChunkPos,
                callback -> {
                    var recordedMarkedPoses = recordMap(new Int2ObjectOpenHashMap<>(), markedPoses.get(packedChunkPos));
                    callback.task = ()->()->{
                        resetChunk(packedChunkPos, recordedMarkedPoses);
                        return ChunkedTaskInstance.CallbackStatus.CONTINUE;
                    };
                }
            );
        }
    }
    
    // 更新某pos的渲染面状态
    private void updatePosRender(long packedBlockPos){
        UpdateCounter.updated();
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
				//noinspection resource
				quads[o[0]] = shapeRegister.register(new Quad(x + o[4], y + o[5], z + o[6], o[7], o[8], o[9], o[10], o[11], o[12], color, useCullFace));
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
                    UpdateCounter.updated();
                }
            }
        }
    }
    
    void updatePosesNeedToUpdate(double chunkedCamX, double chunkedCamZ) {
        if(UpdateCounter.isTired()) return;
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
                    if(!UpdateCounter.updated()) return;
                }
                posesNeedToUpdateRender.remove(chunksNeedToUpdate.dequeueLong());
            }
        }
    }
    
    TaskResult generateFullTaskResult(long packedChunkPos, Int2ObjectOpenHashMap<MutableInt> markedPoses, @Nullable ShapeList rangeLimit, IntOpenHashSet basePosesNeedToUpdate) {
        var taskResult = new TaskResult(markedPoses, new IntOpenHashSet(), basePosesNeedToUpdate);
        buildShownPoses(taskResult.shownPoses, rangeLimit, taskResult.markedPoses.keySet(), packedChunkPos);
        markEdgePoses(taskResult.shownPoses, taskResult.shownPoses, taskResult.posesNeedToUpdate);
        return taskResult;
    }
    
    void registerAll(boolean b){
        Registries.AFTER_CLIENT_WORLD_CHANGE.register(this, b);
        Registries.BETWEEN_RENDER_FRAMES.register(this, b);
    }
    
    private ChunkedTaskInstance.CallbackStatus resetChunk(long packedChunkPos, TaskResult res) {
        this.markedPoses.put(packedChunkPos, res.markedPoses);
        renderingPoses.put(packedChunkPos, res.shownPoses);
        posesNeedToUpdateRender.put(packedChunkPos, combineCollections(res.posesNeedToUpdate, posesNeedToUpdateRender.get(packedChunkPos)));
        return ChunkedTaskInstance.CallbackStatus.CONTINUE;
    }
    
    private static void buildShownPoses(IntOpenHashSet shownPoses, @Nullable ShapeList rangeLimit, IntSet chunkLocalMarkedPoses, long packedChunkPos) {
        int chunkBlockX = Packed.getBlockCoord(Packed.ChunkPos.unpackX(packedChunkPos));
        int chunkBlockZ = Packed.getBlockCoord(Packed.ChunkPos.unpackZ(packedChunkPos));
        var it = chunkLocalMarkedPoses.iterator();
        while (it.hasNext()) {
            int localPos = it.nextInt();
            int x = chunkBlockX + Packed.ChunkLocal.unpackX(localPos);
            int y = Packed.ChunkLocal.unpackY(localPos);
            int z = chunkBlockZ + Packed.ChunkLocal.unpackZ(localPos);
            if(rangeLimit == null || rangeLimit.testPos(x, y, z))
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
}
