package lpctools.tools.slightXRay;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.ChunkedTaskInstance;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.BlockOuterEdgeHighlightInstance;
import lpctools.tools.ToolUtils;
import lpctools.util.AlgorithmUtils;
import lpctools.util.Packed;
import lpctools.util.instance.CameraPosMarker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
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
    private final CameraPosMarker cameraPosMarker = new CameraPosMarker();
    
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
    
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, @Nullable BlockState lastState, @Nullable BlockState newState) {
        if(newState == null) newState = Blocks.AIR.getDefaultState();
        if(isFluid(newState.getBlock())) return;
        if(doShowAround(newState)){
            for(BlockPos pos1 : iterateInManhattanDistance(pos, 2))
                testPos(chunk.getWorld(), pos1);
        }
        else testPos(chunk.getWorld(), pos);
    }
    
    @Override public void onChunkLoad(@NonNull ClientWorld world, @NonNull WorldChunk chunk) {
        ChunkPos pos = chunk.getPos();
        testChunkAsync(pos.x, pos.z, world);
        testChunkAsync(pos.x - 1, pos.z, world);
        testChunkAsync(pos.x + 1, pos.z, world);
        testChunkAsync(pos.x, pos.z - 1, world);
        testChunkAsync(pos.x, pos.z + 1, world);
    }
    
    @Override public void afterWorldChange(@NonNull MinecraftClient minecraftClient, @NonNull ClientWorld clientWorld) {
        testWorldAsync(clientWorld);
    }
    
    @Override public void close() {
        registerAll(false);
        highlightInstance.close();
        taskInstance.close();
    }
    
    private static boolean doShowAround(BlockState state){ return !state.isOpaque() || state.isTransparent(); }
    
    void setRangeLimit(ShapeList rangeLimit) {
        highlightInstance.setRangeLimit(rangeLimit);
    }
    
    void setUseCullFace(boolean useCullFace) {
        highlightInstance.setUseCullFace(useCullFace);
    }
    
    void updateRangeLimit() { setRangeLimit(displayRange.buildShapeList()); }
    void updateUseCullFace() { setUseCullFace(useCullFace.getAsBoolean()); }
    
    // 检测pos处是否需要进行标记并更新标记
    private void testPos(World world, BlockPos pos){
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
    
    private void testChunkAsync(int chunkX, int chunkZ, ClientWorld world){
        long packedChunkPos = Packed.ChunkPos.pack(chunkX, chunkZ);
        ChunkData task = ChunkData.buildData(chunkX, chunkZ, world);
        if(task != null) taskInstance.scheduleTask(packedChunkPos, callback->buildAsyncTask(packedChunkPos, task, callback));
    }
    
    private void buildAsyncTask(long packedChunkPos, ChunkData chunkData, ChunkedTaskInstance.DelayedCallback callback){
        HashMap<Block, MutableInt> recorded = SlightXRayData.getRecordedXRayBlocks();
        callback.task = ()->{
            var res = testChunkData(chunkData, recorded);
            return ()->{
                highlightInstance.resetChunk(packedChunkPos, res);
                return ChunkedTaskInstance.CallbackStatus.CONTINUE;
            };
        };
    }
	
	public void resetData() { testWorldAsync(MinecraftClient.getInstance().world); }
    public void refreshColor() { highlightInstance.reshapesAsync(); }
    
    private void testWorldAsync(ClientWorld world){
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
        var camPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        cameraPosMarker.nextPos(ToolUtils.chunkedCoord(camPos.x), ToolUtils.chunkedCoord(camPos.z));
        clearChunksOutOfRange(cameraPosMarker.getResX(), cameraPosMarker.getResZ(),
            MathHelper.square(cameraPosMarker.getResR() + 2 * MinecraftClient.getInstance().options.getViewDistance().getValue()));
    }
    
    private record ChunkData(Chunk current, Chunk west, Chunk east, Chunk north, Chunk south){
        static @Nullable ChunkData buildData(int chunkX, int chunkZ, ClientWorld world){
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
    
    private static Int2ObjectOpenHashMap<MutableInt> testChunkData(ChunkData data, HashMap<Block, MutableInt> colorMap){
        var res = new Int2ObjectOpenHashMap<MutableInt>();
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
                res.put(chunkLocalPos, color);
                continue;
            }
            for(Direction direction : Direction.values()){
                if(displaysNear.get(pos1.getX() + direction.getOffsetX(), pos1.getY() + direction.getOffsetY(), pos1.getZ() + direction.getOffsetZ())){
                    int chunkLocalPos = Packed.ChunkLocal.pack(pos1);
                    res.put(chunkLocalPos, color);
                    break;
                }
            }
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
