package lpctools.tools.slightXRay;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.render.translucentShapes.Quad;
import lpctools.lpcfymasaapi.render.translucentShapes.TranslucentShapes;
import lpctools.util.AlgorithmUtils;
import lpctools.util.MathUtils;
import lpctools.util.Packed;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static lpctools.generic.GenericConfigs.updateLimitPerFrame;
import static lpctools.tools.slightXRay.SlightXRayData.*;
import static lpctools.util.AlgorithmUtils.iterateInManhattanDistance;
import static lpctools.util.BlockUtils.isFluid;

class DataInstance implements AutoCloseable, ClientChunkEvents.Load, ClientWorldEvents.AfterClientWorldChange, Registries.ClientWorldChunkSetBlockState, Registries.WorldLastRender {
    private final ArrayList<UpdateData> runningTasks = new ArrayList<>();
    private final ArrayList<Pair<ChunkPos, Supplier<UpdateData>>> delayedTasks = new ArrayList<>();
    // 为方便清理操作，给markedPoses分块，区块坐标->区块local坐标->颜色
    private final Long2ObjectOpenHashMap<Int2ObjectOpenHashMap<MutableInt>> markedPoses = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectOpenHashMap<TranslucentShapes> posQuads = new Long2ObjectOpenHashMap<>();
    private final LongOpenHashSet posesNeedToUpdateRender = new LongOpenHashSet();
	private final int runningTasksLimit = Runtime.getRuntime().availableProcessors();
    
    private int updateCounter = 0;
    
    MutableInt getMarkedPos(long packedBlockPos){
        long packedChunkPos = Packed.ChunkPos.packedFromBlockPos(packedBlockPos);
        var markedPosesChunk = markedPoses.getOrDefault(packedChunkPos, null);
        if(markedPosesChunk == null) return null;
        int packedChunkLocal = Packed.ChunkLocal.packedFromBlockPos(packedBlockPos);
        return markedPosesChunk.getOrDefault(packedChunkLocal, null);
    }
    
    @SuppressWarnings("unused")
    MutableInt getMarkedPos(int x, int y, int z){
        long packedChunkPos = Packed.ChunkPos.packCoords(x, z);
        var markedPosesChunk = markedPoses.getOrDefault(packedChunkPos, null);
        if(markedPosesChunk == null) return null;
        int packedChunkLocal = Packed.ChunkLocal.pack(x, y, z);
        return markedPosesChunk.getOrDefault(packedChunkLocal, null);
    }
    
    boolean containsMarkedPos(long packedBlockPos){
        long packedChunkPos = Packed.ChunkPos.packedFromBlockPos(packedBlockPos);
        var markedPosesChunk = markedPoses.getOrDefault(packedChunkPos, null);
        if(markedPosesChunk == null) return false;
        return markedPosesChunk.containsKey(Packed.ChunkLocal.packedFromBlockPos(packedBlockPos));
    }
    
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean containsMarkedPos(int x, int y, int z){
        long packedChunkPos = Packed.ChunkPos.packCoords(x, z);
        var markedPosesChunk = markedPoses.getOrDefault(packedChunkPos, null);
        if(markedPosesChunk == null) return false;
        return markedPosesChunk.containsKey(Packed.ChunkLocal.pack(x, y, z));
    }
    
    MutableInt putMarkedPos(long packedBlockPos, MutableInt colorMarker){
        long packedChunkPos = Packed.ChunkPos.packedFromBlockPos(packedBlockPos);
        var markedPosesChunk = markedPoses.computeIfAbsent(packedChunkPos, p->new Int2ObjectOpenHashMap<>());
        return markedPosesChunk.put(Packed.ChunkLocal.packedFromBlockPos(packedBlockPos), colorMarker);
    }
    
    @SuppressWarnings("unused")
    MutableInt putMarkedPos(int x, int y, int z, MutableInt colorMarker){
        long packedChunkPos = Packed.ChunkPos.packCoords(x, z);
        var markedPosesChunk = markedPoses.computeIfAbsent(packedChunkPos, p->new Int2ObjectOpenHashMap<>());
        return markedPosesChunk.put(Packed.ChunkLocal.pack(x, y, z), colorMarker);
    }
    
    // 不进行空区块清理操作，统一清理时再做
    MutableInt removeMarkedPos(long packedBlockPos){
        long packedChunkPos = Packed.ChunkPos.packedFromBlockPos(packedBlockPos);
        var markedPosesChunk = markedPoses.getOrDefault(packedChunkPos, null);
        if(markedPosesChunk == null) return null;
        return markedPosesChunk.remove(Packed.ChunkLocal.packedFromBlockPos(packedBlockPos));
    }
    
    @SuppressWarnings("unused")
    MutableInt removeMarkedPos(int x, int y, int z){
        long packedChunkPos = Packed.ChunkPos.packCoords(x, z);
        var markedPosesChunk = markedPoses.getOrDefault(packedChunkPos, null);
        if(markedPosesChunk == null) return null;
        return markedPosesChunk.remove(Packed.ChunkLocal.pack(x, y, z));
    }
    
    // 更新某pos的渲染面状态
    private void updatePosRender(long packedBlockPos){
        --updateCounter;
        var colorSource = getMarkedPos(packedBlockPos);
        if(colorSource == null) {
            var old = posQuads.remove(packedBlockPos);
            if(old != null) old.close();
            return;
        }
        
        int x = BlockPos.unpackLongX(packedBlockPos);
        int y = BlockPos.unpackLongY(packedBlockPos);
        int z = BlockPos.unpackLongZ(packedBlockPos);
        int color = colorSource.intValue();
        var q = posQuads.remove(packedBlockPos);
        TranslucentShapes quads = q == null ? new TranslucentShapes(false, true) : q;
        quads.clear();
        if(!containsMarkedPos(x - 1, y, z)) quads.addQuad(x    , y, z, 0, 0, 1, 0, 1, 0, color);
        if(!containsMarkedPos(x + 1, y, z)) quads.addQuad(x + 1, y, z, 0, 1, 0, 0, 0, 1, color);
        if(!containsMarkedPos(x, y - 1, z)) quads.addQuad(x, y    , z, 1, 0, 0, 0, 0, 1, color);
        if(!containsMarkedPos(x, y + 1, z)) quads.addQuad(x, y + 1, z, 0, 0, 1, 1, 0, 0, color);
        if(!containsMarkedPos(x, y, z - 1)) quads.addQuad(x, y, z    , 0, 1, 0, 1, 0, 0, color);
        if(!containsMarkedPos(x, y, z + 1)) quads.addQuad(x, y, z + 1, 1, 0, 0, 0, 1, 0, color);
        if(!quads.isEmpty()) {
            quads.trim();
            posQuads.put(packedBlockPos, quads);
        }
        else quads.close();
    }
    
    // 将某pos标记为需要更新渲染面
    // private void markNeedUpdateRender(long packedBlockPos){ posesNeedToUpdateRender.add(packedBlockPos); }
    
    // 将某pos及其相邻pos标记为需要更新渲染面
    private void markPosAndNeighboursNeedUpdateRender(long packedBlockPos){
        int x = Packed.BlockPos.unpackX(packedBlockPos);
        int y = Packed.BlockPos.unpackY(packedBlockPos);
        int z = Packed.BlockPos.unpackZ(packedBlockPos);
        posesNeedToUpdateRender.add(packedBlockPos);
        posesNeedToUpdateRender.add(Packed.BlockPos.pack(x - 1, y, z));
        posesNeedToUpdateRender.add(Packed.BlockPos.pack(x + 1, y, z));
        posesNeedToUpdateRender.add(Packed.BlockPos.pack(x, y - 1, z));
        posesNeedToUpdateRender.add(Packed.BlockPos.pack(x, y + 1, z));
        posesNeedToUpdateRender.add(Packed.BlockPos.pack(x, y, z - 1));
        posesNeedToUpdateRender.add(Packed.BlockPos.pack(x, y, z + 1));
    }
    
    // 清理超出距离的区块
    private void clearChunksOutOfRange(double camX, double camZ) {
        double distanceLimitSquared = MathHelper.square(2.0 * MinecraftClient.getInstance().options.getViewDistance().getValue());
        double px = camX / 16 - 0.5, pz = camZ / 16 - 0.5;
        var it = markedPoses.long2ObjectEntrySet().fastIterator();
        while(it.hasNext()){
            var entry = it.next();
            var marked = entry.getValue();
            if(marked.isEmpty()){
                it.remove();
                continue;
            }
            long packedChunkPos = entry.getLongKey();
            int x = Packed.ChunkPos.unpackX(packedChunkPos);
            int z = Packed.ChunkPos.unpackZ(packedChunkPos);
            double distanceSquared = (px - x) * (px - x) + (pz - z) * (pz - z);
            if(distanceSquared >= distanceLimitSquared){
                it.remove();
                var _it = marked.int2ObjectEntrySet().fastIterator();
                while(_it.hasNext()){
                    var e = _it.next();
                    var q = posQuads.remove(Packed.BlockPos.packedFromChunkLocal(packedChunkPos, e.getIntKey()));
                    if(q != null) {
                        q.close();
                        --updateCounter;
                    }
                }
                updateCounter -= marked.size() / 16;
                if(updateCounter <= 0) break;
            }
        }
    }
    
    void updatePosesNeedToUpdate() {
        if(!posesNeedToUpdateRender.isEmpty()) {
            var it = posesNeedToUpdateRender.iterator();
            while (it.hasNext()) {
                updatePosRender(it.nextLong());
                it.remove();
                if (updateCounter <= 0) break;
            }
        }
    }
    
    @Override public void onLast(Registries.MASAWorldRenderContext context) {
        updateCounter = updateLimitPerFrame.getAsInt() + Math.min(updateCounter, 0);
        SlightXRay.tryRefreshXRayBlocks();
        var camPos = context.camera().getPos();
        clearChunksOutOfRange(camPos.x, camPos.z);
        if(updateCounter <= 0) return;
        updatePosesNeedToUpdate();
        if(updateCounter <= 0) return;
        LongOpenHashSet completedFutures = null;
        if(!delayedTasks.isEmpty()){
            delayedTasks.sort(Comparator.<Pair<ChunkPos, Supplier<UpdateData>>>comparingDouble(v->squaredDistanceByClient(v.getLeft())).reversed());
            while(!delayedTasks.isEmpty() && runningTasks.size() < runningTasksLimit)
                runningTasks.add(delayedTasks.removeLast().getRight().get());
        }
        runningTasks.sort(Comparator.comparingDouble(v->squaredDistanceByClient(v.pos)));
        for(UpdateData data : runningTasks){
            if(!data.future.isDone()) continue;
            if(completedFutures == null) completedFutures = new LongOpenHashSet();
            var result = data.future.join();
            for(var v : result.markedPoses.long2ObjectEntrySet())
                putMarkedPos(v.getLongKey(), v.getValue());
            updateCounter -= result.markedPoses.size() / 16;
            result.markedPoses.clear();
            if(updateCounter <= 0) break;
            var it = result.posesNeedToUpdate.long2ObjectEntrySet().iterator();
            while(it.hasNext()) {
                var entry = it.next();
                var cache = entry.getValue();
                long packedPos = entry.getLongKey();
                it.remove();
                int x = BlockPos.unpackLongX(packedPos);
                int y = BlockPos.unpackLongY(packedPos);
                int z = BlockPos.unpackLongZ(packedPos);
                for(var e : cache.preparedPairs){
                    var d = e.direction;
                    long oppositePos = BlockPos.asLong(x + d.getOffsetX(), y + d.getOffsetY(), z + d.getOffsetZ());
                    if(containsMarkedPos(oppositePos)) {
                        var opposite = posQuads.getOrDefault(oppositePos, null);
                        if(opposite != null) updatePosRender(oppositePos);
                    }
                    else cache.quads.addQuad(e.quad, false);
                }
                TranslucentShapes old;
                if(cache.quads.isEmpty()) {
                    old = posQuads.remove(packedPos);
                    cache.quads.close();
                }
                else old = posQuads.put(packedPos, cache.quads);
                if(old != null) old.close();
                if(--updateCounter <= 0) break;
            }
            if(result.posesNeedToUpdate.isEmpty()) completedFutures.add(data.pos.toLong());
            else break;
        }
        if(completedFutures != null && !completedFutures.isEmpty()) {
            LongOpenHashSet finalCompletedFutures = completedFutures;
            AlgorithmUtils.fastRemove(runningTasks, v -> finalCompletedFutures.contains(v.pos.toLong()));
            completedFutures.clear();
        }
    }
    public final MinecraftClient client;
    public double squaredDistanceByClient(ChunkPos chunkPos){
        if(client.player != null) return MathUtils.squaredDistance(client.player.getEyePos(), chunkPos);
        else return MathUtils.square(client.options.getViewDistance().getValue() * 16);
    }
    void registerAll(boolean b){
        Registries.AFTER_CLIENT_WORLD_CHANGE.register(this, b);
        Registries.MASA_WORLD_RENDER_LAST.register(this, b);
        Registries.CLIENT_CHUNK_LOAD.register(this, b);
        Registries.CLIENT_WORLD_CHUNK_SET_BLOCK_STATE.register(this, b);
    }
    DataInstance(MinecraftClient client){
        this.client = client;
        registerAll(true);
        resetData();
    }
    
    public void resetData(){
        ClientWorld world = client.world;
        ClientPlayerEntity player = client.player;
        if(player == null || world == null) return;
        Vec3d playerEyePos = player.getEyePos();
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, playerEyePos)){
            ChunkPos chunkPos = chunk.getPos();
            testChunkAsync(world, chunkPos, MathUtils.squaredDistance(playerEyePos, chunkPos));
        }
    }
    @Override public void close(){
        registerAll(false);
        clearData();
    }
    @Override public void onChunkLoad(ClientWorld world, WorldChunk chunk) {
        ChunkPos pos = chunk.getPos();
        double distanceSquare;
        if(client.player != null) distanceSquare = MathUtils.squaredDistance(client.player.getEyePos(), chunk.getPos());
        else distanceSquare = MathUtils.square(client.options.getViewDistance().getValue() * 16);
        testChunkAsync(world, pos, distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x - 1, pos.z), distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x + 1, pos.z), distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x, pos.z - 1), distanceSquare);
        testChunkAsync(world, new ChunkPos(pos.x, pos.z + 1), distanceSquare);
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
        AlgorithmUtils.cancelTasks(runningTasks, v->v.future);
        delayedTasks.clear();
        posQuads.values().forEach(TranslucentShapes::close);
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
        MutableInt old;
        if(res == null) old = removeMarkedPos(packedBlockPos);
        else old = putMarkedPos(packedBlockPos, res);
		if(old != null || res != null) markPosAndNeighboursNeedUpdateRender(packedBlockPos);
    }
    
    private static boolean doShowAround(BlockState state){ return !state.isOpaque() || state.isTransparent(); }
    
    // 能由子线程处理的尽量给子线程处理，此处尝试将Quad的new操作也交给子线程
    record DirectionQuadPair(Direction direction, Quad quad) {}
    record UpdatedCache(ArrayList<DirectionQuadPair> preparedPairs, TranslucentShapes quads) {}
    record TaskResult(Long2ObjectOpenHashMap<MutableInt> markedPoses, Long2ObjectOpenHashMap<UpdatedCache> posesNeedToUpdate){}
    private record UpdateData(ChunkPos pos, CompletableFuture<TaskResult> future, double distanceSquare){}
    private void testChunkAsync(ClientWorld world, ChunkPos pos, double distanceSquare){
        ChunkTask task = ChunkTask.buildTask(world, pos);
        if(task != null) {
            HashMap<Block, MutableInt> copy;
            synchronized (XRayBlocks){copy = new HashMap<>(XRayBlocks);}
            delayedTasks.add(new Pair<>(pos, ()->new UpdateData(pos, GenericUtils.supplyAsync(()->task.testCurrentChunk(copy), distanceSquare), distanceSquare)));
        }
    }
    private record ChunkTask(ChunkPos pos, Chunk current, Chunk west, Chunk east, Chunk north, Chunk south){
        static @Nullable DataInstance.ChunkTask buildTask(ClientWorld world, ChunkPos pos){
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
        public TaskResult testCurrentChunk(HashMap<Block, MutableInt> colorMap){
            TaskResult res = new TaskResult(new Long2ObjectOpenHashMap<>(), new Long2ObjectOpenHashMap<>());
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
                if(displaysNear.get(pos1)){
                    res.markedPoses.put(BlockPos.add(pos1.asLong(), ChunkSectionPos.getBlockCoord(pos.x), 0, ChunkSectionPos.getBlockCoord(pos.z)), color);
                    continue;
                }
                for(Direction direction : Direction.values()){
                    if(displaysNear.get(pos1.offset(direction))){
                        res.markedPoses.put(BlockPos.add(pos1.asLong(), ChunkSectionPos.getBlockCoord(pos.x), 0, ChunkSectionPos.getBlockCoord(pos.z)), color);
                        break;
                    }
                }
            }
            class Temp{
                static void tryAdd(Long2ObjectOpenHashMap<UpdatedCache> res, Long2ObjectOpenHashMap<MutableInt> testMap, long packedPos){
                    int x = BlockPos.unpackLongX(packedPos);
                    int y = BlockPos.unpackLongY(packedPos);
                    int z = BlockPos.unpackLongZ(packedPos);
                    var colorContainer = testMap.getOrDefault(packedPos, null);
                    if(colorContainer == null) return;
                    int color = colorContainer.intValue();
                    for(var d : Direction.values()){
                        if(!testMap.containsKey(BlockPos.asLong(x + d.getOffsetX(), y + d.getOffsetY(), z + d.getOffsetZ()))){
                            var cache = res.computeIfAbsent(packedPos, v->new UpdatedCache(new ArrayList<>(), new TranslucentShapes(false, true)));
                            cache.preparedPairs.add(new DirectionQuadPair(d, switch(d) {
                                case DOWN -> new Quad(x, y    , z, 1, 0, 0, 0, 0, 1, color);
                                case UP   -> new Quad(x, y + 1, z, 0, 0, 1, 1, 0, 0, color);
                                case NORTH-> new Quad(x, y, z    , 0, 1, 0, 1, 0, 0, color);
                                case SOUTH-> new Quad(x, y, z + 1, 1, 0, 0, 0, 1, 0, color);
                                case WEST -> new Quad(x    , y, z, 0, 0, 1, 0, 1, 0, color);
                                case EAST -> new Quad(x + 1, y, z, 0, 1, 0, 0, 0, 1, color);
                            }));
                        }
                    }
                    var cache = res.getOrDefault(packedPos, null);
                    if(cache != null) {
                        cache.preparedPairs.trimToSize();
                        cache.quads.ensureCapacity(cache.preparedPairs.size());
                    }
                }
            }
            var resSet = res.posesNeedToUpdate;
            res.markedPoses.keySet().forEach(p->Temp.tryAdd(resSet, res.markedPoses, p));
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
