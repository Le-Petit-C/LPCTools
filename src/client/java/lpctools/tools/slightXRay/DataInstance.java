package lpctools.tools.slightXRay;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.ChunkedTaskInstance;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.BlockOuterEdgeHighlightInstance;
import lpctools.tools.ToolUtils;
import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.Packed;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;

import static lpctools.tools.slightXRay.SlightXRay.displayRange;
import static lpctools.tools.slightXRay.SlightXRay.useCullFace;
import static lpctools.util.AlgorithmUtils.iterateInManhattanDistance;
import static lpctools.util.BlockUtils.isFluid;
import static lpctools.util.DataUtils.loadedChunks;

class DataInstance implements AutoCloseable, ClientChunkEvents.Load, ClientWorldEvents.AfterClientWorldChange, Registries.ClientWorldChunkSetBlockState, Registries.BetweenRenderFrames {
    private final BlockOuterEdgeHighlightInstance highlightInstance = new BlockOuterEdgeHighlightInstance();
    private final ChunkedTaskInstance taskInstance = new ChunkedTaskInstance();
    
    DataInstance() {
        updateRangeLimit();
        updateUseCullFace();
        registerAll(true);
        resetData();
    }
    
    void registerAll(boolean b){
        Registries.AFTER_CLIENT_WORLD_CHANGE.register(this, b);
        Registries.CLIENT_CHUNK_LOAD.register(this, b);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
        Registries.BETWEEN_RENDER_FRAMES.register(this, b);
    }
    
    @Override public void onClientWorldChunkSetBlockState(LevelChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState) {
        if(newState == null) newState = Blocks.AIR.defaultBlockState();
        if(isFluid(newState.getBlock())) return;
        if(doShowAround(newState)){
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2))
                testPos(chunk.getLevel(), pos1);
        }
        else testPos(chunk.getLevel(), pos);
    }
    
    @Override public void onChunkLoad(@NonNull ClientLevel world, @NonNull LevelChunk chunk) {
        ChunkPos pos = chunk.getPos();
        testChunkAsync(pos.x, pos.z, world);
        testChunkAsync(pos.x - 1, pos.z, world);
        testChunkAsync(pos.x + 1, pos.z, world);
        testChunkAsync(pos.x, pos.z - 1, world);
        testChunkAsync(pos.x, pos.z + 1, world);
    }
    
    @Override public void afterWorldChange(@NonNull Minecraft minecraftClient, @NonNull ClientLevel clientWorld) {
        testWorldAsync(clientWorld);
    }
    
    @Override public void close() {
        registerAll(false);
        highlightInstance.close();
        taskInstance.close();
    }
    
    private static boolean doShowAround(BlockState state){ return !state.canOcclude() || state.propagatesSkylightDown(); }
    
    void setRangeLimit(ShapeList rangeLimit) {
        highlightInstance.setRangeLimit(rangeLimit);
    }
    
    void setUseCullFace(boolean useCullFace) {
        highlightInstance.setUseCullFace(useCullFace);
    }
    
    void updateRangeLimit() { setRangeLimit(displayRange.buildShapeList()); }
    void updateUseCullFace() { setUseCullFace(useCullFace.getAsBoolean()); }
    
    // 检测pos处是否需要进行标记并更新标记
    private void testPos(Level world, BlockPos pos){
        long packedBlockPos = pos.asLong();
        BlockState state = world.getBlockState(pos);
        MutableInt color;
        color = SlightXRayData.XRayBlocks.getOrDefault(state.getBlock(), null);
        
        MutableInt res = null;
        if(color != null) {
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2)) {
                if(doShowAround(world.getBlockState(pos1))){
                    res = color;
                    break;
                }
            }
        }
        highlightInstance.mark(packedBlockPos, res);
    }
    
    private void testChunkAsync(int chunkX, int chunkZ, ClientLevel world){
        long packedChunkPos = Packed.ChunkPos.pack(chunkX, chunkZ);
        ChunkData task = ChunkData.buildData(chunkX, chunkZ, world);
        if(task != null) taskInstance.scheduleTask(packedChunkPos, callback->buildAsyncTask(packedChunkPos, task, callback));
    }
    
    private void buildAsyncTask(long packedChunkPos, ChunkData chunkData, ChunkedTaskInstance.DelayedCallback callback){
        HashMap<Block, MutableInt> recorded = SlightXRayData.getRecordedXRayBlocks();
        Int2ObjectOpenHashMap<MutableInt> recordedMarkedPoses = ToolUtils.recordMap(new Int2ObjectOpenHashMap<>(), highlightInstance.getChunkMarks(packedChunkPos));
        callback.task = ()->{
            var res = testChunkData(chunkData, recorded, recordedMarkedPoses);
            return ()->{
                highlightInstance.resetChunk(packedChunkPos, res);
                return ChunkedTaskInstance.CallbackStatus.CONTINUE;
            };
        };
    }
	
	public void resetData() { testWorldAsync(Minecraft.getInstance().level); }
    public void refreshColor() { highlightInstance.reshapesAsync(); }
    
    private void testWorldAsync(ClientLevel world){
        highlightInstance.clearData();
        taskInstance.clearTasks();
        if(world == null) return;
        for(var worldChunk : loadedChunks(world)) {
            var pos = worldChunk.getPos();
            testChunkAsync(pos.x, pos.z, world);
        }
    }
    
    private void clearChunksOutOfRange(double chunkedCamX, double chunkedCamZ, double chunkedDistanceSquared){
        taskInstance.clearTasksOutOfRange(chunkedCamX, chunkedCamZ, chunkedDistanceSquared);
        highlightInstance.clearChunksOutOfRange(chunkedCamX, chunkedCamZ, chunkedDistanceSquared);
    }
    
    @Override public void betweenFrames() {
        SlightXRay.tryRefreshXRayBlocks();
        DataUtils.executeWithRenderCenterPos((x, z, r)->clearChunksOutOfRange(x, z, r * r),
            2 * Minecraft.getInstance().options.renderDistance().get());
    }
    
    private record ChunkData(ChunkAccess current, ChunkAccess west, ChunkAccess east, ChunkAccess north, ChunkAccess south){
        static @Nullable ChunkData buildData(int chunkX, int chunkZ, ClientLevel world){
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
    
    private static Int2ObjectOpenHashMap<MutableInt> testChunkData(ChunkData data, HashMap<Block, MutableInt> colorMap, Int2ObjectOpenHashMap<MutableInt> recordedMarkedPoses){
        var res = new Int2ObjectOpenHashMap<MutableInt>();
        int bottom = data.current.getMinY(), height = data.current.getHeight(), top = bottom + height;
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
        BlockPos.MutableBlockPos posCache = new BlockPos.MutableBlockPos();
        for(BlockPos pos1 : AlgorithmUtils.iterateInBox(0, bottom, 0, 15, top - 1, 15)){
            BlockState state = data.current.getBlockState(pos1);
            boolean hasDisplayNear;
            if(displaysNear.get(pos1)) hasDisplayNear = true;
            else {
                boolean b = false;
                for(Direction direction : Direction.values()){
                    if(displaysNear.get(posCache.set(pos1).move(direction))){
                        b = true;
                        break;
                    }
                }
                hasDisplayNear = b;
            }
            int packedChunkLocal = Packed.ChunkLocal.pack(pos1);
            
			MutableInt color;
			if(hasDisplayNear) color = colorMap.get(state.getBlock());
            else color = recordedMarkedPoses.get(packedChunkLocal);
			if(color != null) res.put(packedChunkLocal, color);
		}
        return res;
    }
    
    private record ChunkTestData(int bottomY, boolean[][][] data) {
        private ChunkTestData(int bottomY, int data) {
            this(bottomY, new boolean[18][data + 2][18]);
        }
        boolean get(int x, int y, int z) {return data[x + 1][y - bottomY + 1][z + 1];}
        boolean get(BlockPos pos) {return get(pos.getX(), pos.getY(), pos.getZ());}
        void set(int x, int y, int z, boolean value) {data[x + 1][y - bottomY + 1][z + 1] = value;}
        void set(BlockPos pos, boolean value) {set(pos.getX(), pos.getY(), pos.getZ(), value);}
    }
}
