package lpctools.tools.slightXRay;

import com.google.common.collect.Sets;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.gl.Buffer;
import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.MaskLayer;
import lpctools.lpcfymasaapi.gl.VertexArray;
import lpctools.shader.ShaderPrograms;
import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntSupplier;

//TODO:不使用形状索引而是合并重合顶点的索引（比如相邻矩形）以节省空间
//TODO:顶点缓冲改为相对于区块原点的坐标，减小距离现象影响
//TODO:限制显示范围

public class RenderInstance extends DataInstance implements DataInstance.OnXRayChunkLoadedOrUnloaded, WorldRenderEvents.End, WorldRenderEvents.Start {
    public final SlightXRay parent;
    RenderInstance(SlightXRay parent, MinecraftClient client){
        super(client);
        this.parent = parent;
        ON_XRAY_CHUNK_UPDATED.register(this);
        Registries.WORLD_RENDER_END.register(this);
        Registries.WORLD_RENDER_START.register(this);
    }
    @Override public void close(){
        Registries.WORLD_RENDER_START.unregister(this);
        Registries.WORLD_RENDER_END.unregister(this);
        ON_XRAY_CHUNK_UPDATED.unregister(this);
        for(QuadBuffer buffer : vertexBuffers.values())
            buffer.close();
        vertexBuffers.clear();
        super.close();
    }
    @Override public void onXRayChunkLoadedOrUnloaded(ChunkPos pos, double distanceSquare) {
        HashMap<BlockPos, MutableInt> poses = super.markedPoses.get(pos);
        if(poses == null){
            QuadBuffer buffer = vertexBuffers.remove(pos);
            if(buffer != null) buffer.close();
            return;
        }
        buildQuadBufferAsync(pos, super.markedPoses, distanceSquare);
        buildQuadBufferAsync(new ChunkPos(pos.x - 1, pos.z), super.markedPoses, distanceSquare);
        buildQuadBufferAsync(new ChunkPos(pos.x + 1, pos.z), super.markedPoses, distanceSquare);
        buildQuadBufferAsync(new ChunkPos(pos.x, pos.z - 1), super.markedPoses, distanceSquare);
        buildQuadBufferAsync(new ChunkPos(pos.x, pos.z + 1), super.markedPoses, distanceSquare);
    }
    
    private void removePos(BlockPos pos){
        for(Direction direction : Direction.values()){
            BlockPos nextPos = pos.offset(direction);
            MutableInt nextColor = get(nextPos);
            if(nextColor == null) continue;
            QuadBuffer buffer = vertexBuffers.get(new ChunkPos(nextPos));
            if(buffer == null) continue;
            buffer.quads.add(new RenderQuad(nextPos.toImmutable(), direction.getOpposite(), nextColor));
            buffer.refreshByteBuffer();
        }
        QuadBuffer currentBuffer = vertexBuffers.get(new ChunkPos(pos));
        if(currentBuffer == null) return;
        ArrayList<RenderQuad> quads = currentBuffer.quads;
        AlgorithmUtils.fastRemove(quads, v->v.attachedBlock.equals(pos));
        currentBuffer.refreshByteBuffer();
    }
    private void addPos(BlockPos pos, MutableInt currentColor){
        QuadBuffer currentBuffer = vertexBuffers.get(new ChunkPos(pos));
        ArrayList<RenderQuad> quads = currentBuffer != null ? currentBuffer.quads : null;
        if(quads != null) AlgorithmUtils.fastRemove(quads, v->v.attachedBlock.equals(pos));
        for(Direction direction : Direction.values()){
            BlockPos nextPos = pos.offset(direction);
            MutableInt nextColor = get(nextPos);
            if(nextColor == null && quads != null) {
                quads.add(new RenderQuad(pos.toImmutable(), direction, currentColor));
                continue;
            }
            QuadBuffer buffer = vertexBuffers.get(new ChunkPos(nextPos));
            if(buffer == null) continue;
            ArrayList<RenderQuad> nextQuads = buffer.quads;
            Direction opposite = direction.getOpposite();
            AlgorithmUtils.fastRemove(nextQuads, v->v.attachedBlock.equals(nextPos) && v.direction.equals(opposite));
            buffer.refreshByteBuffer();
        }
        if(currentBuffer != null) currentBuffer.refreshByteBuffer();
    }
    
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState) {
        HashMap<BlockPos, MutableInt> lastContained = new HashMap<>();
        for(BlockPos pos1 : AlgorithmUtils.iterateInManhattanDistance(pos, 2)){
            MutableInt color = get(pos1);
            if(color != null) lastContained.put(pos1.toImmutable(), color);
        }
        super.onClientWorldChunkSetBlockState(chunk, pos, lastState, newState);
        for(BlockPos pos1 : AlgorithmUtils.iterateInManhattanDistance(pos, 2)){
            MutableInt currentColor = get(pos1);
            if(Objects.equals(lastContained.get(pos1), currentColor)) continue;
            if(currentColor == null) removePos(pos1);
            else addPos(pos1, currentColor);
            QuadBuffer quadBuffer = vertexBuffers.get(new ChunkPos(pos1));
            if(quadBuffer != null) quadBuffer.lastUpdatePos = null;
        }
    }
    
    @Override public void afterWorldChange(MinecraftClient mc, ClientWorld world) {
        super.afterWorldChange(mc, world);
        AlgorithmUtils.cancelTasks(asyncQuadBufferBuilders, Pair::getRight);
        for(QuadBuffer buffer : vertexBuffers.values()) buffer.close();
        vertexBuffers.clear();
    }
    
    @Override public void onStartTick(MinecraftClient mc){
        super.onStartTick(mc);
        HashSet<ChunkPos> completedFutures = new HashSet<>();
        for(Pair<ChunkPos, CompletableFuture<QuadBuffer>> entry : asyncQuadBufferBuilders){
            if(!entry.getRight().isDone()) continue;
            completedFutures.add(entry.getLeft());
            QuadBuffer old = vertexBuffers.put(entry.getLeft(), entry.getRight().join());
            if(old != null) old.close();
        }
        AlgorithmUtils.fastRemove(asyncQuadBufferBuilders, v->completedFutures.contains(v.getLeft()));
    }
    private void sort(ArrayList<RenderQuad> quads, Vec3d cam){
        quads.sort(Comparator.<RenderQuad>comparingDouble(quad -> quad.centerPos.squaredDistanceTo(cam)).reversed());
    }
    private IntSupplier asyncPrepareRenderChunk(QuadBuffer quadBuffer, Vec3d camPos){
        sort(quadBuffer.quads, camPos);
        for(RenderQuad quad : quadBuffer.quads)
            quad.vertex(quadBuffer.byteBuffer);
        quadBuffer.byteBuffer.flip();
        return ()->{
            quadBuffer.dataAndBind(indexBuffer);
            return quadBuffer.quads.size();
        };
    }
    private RenderPrepareResult asyncPrepareRenderMain(Vec3d camPos){
        int maxShapeCount = 0;
        ChunkPos chunkPos = new ChunkPos(BlockPos.ofFloored(camPos));
        ArrayList<CompletableFuture<IntSupplier>> renderBufferBuilders = new ArrayList<>();
        for(Vector2i vec : AlgorithmUtils.iterateFromFurthestInDistance(new Vector2i(chunkPos.x, chunkPos.z), client.options.getViewDistance().getValue())){
            ChunkPos pos = new ChunkPos(vec.x, vec.y);
            QuadBuffer buffer = vertexBuffers.get(pos);
            if(buffer == null || buffer.quads.isEmpty()) continue;
            if(buffer.quads.size() > maxShapeCount) maxShapeCount = buffer.quads.size();
            if(buffer.shouldUpdate(camPos) || buffer.vertexArray == null)
                renderBufferBuilders.add(CompletableFuture.supplyAsync(()->asyncPrepareRenderChunk(buffer, camPos)));
            else renderBufferBuilders.add(CompletableFuture.completedFuture(()->{
                buffer.vertexArray.bind();
                return buffer.quads.size();
            }));
        }
        return new RenderPrepareResult(renderBufferBuilders, maxShapeCount);
    }
    //futures中的IntSupplier应执行的操作为绑定buffer并返回shapeCount
    private record RenderPrepareResult(ArrayList<CompletableFuture<IntSupplier>> futures, int maxShapeCount){}
    //返回值是最大形状数量
    CompletableFuture<RenderPrepareResult> renderTask;
    @Override public void onStart(WorldRenderContext context){
        Vec3d camPos = context.camera().getPos();
        renderTask = CompletableFuture.supplyAsync(()->asyncPrepareRenderMain(camPos));
    }
    @Override public void onEnd(WorldRenderContext context) {
        RenderPrepareResult result = renderTask.join();
        ensureIndexBufferSize(result.maxShapeCount);
        try(MaskLayer layer = new MaskLayer()){
            layer.enableBlend().enableCullFace(parent.useCullFace.getBooleanValue()).disableDepthTest();
            ShaderPrograms.PositionColorProgram program = ShaderPrograms.POSITION_COLOR_PROGRAM;
            Matrix4f mat = MathUtils.inverseOffsetMatrix4f(context.camera().getPos().toVector3f());
            context.positionMatrix().mul(mat, mat);
            context.projectionMatrix().mul(mat, mat);
            program.setFinalMatrix(mat);
            program.useAndUniform();
            for(CompletableFuture<IntSupplier> future : result.futures){
                int shapeCount = future.join().getAsInt();
                Constants.DrawMode.TRIANGLES.drawElements(shapeCount * 6, Constants.IndexType.INT);
            }
            VertexArray.unbindStatic();
        }
    }
    private static class QuadBuffer implements AutoCloseable{
        public static final ShaderPrograms.PositionColorProgram program = ShaderPrograms.POSITION_COLOR_PROGRAM;
        public final ArrayList<RenderQuad> quads;
        public final ChunkPos thisPos;
        public @NotNull ByteBuffer byteBuffer;
        public @Nullable Buffer vertexBuffer;
        public @Nullable VertexArray vertexArray;
        public @Nullable Vec3d lastUpdatePos;
        QuadBuffer(ArrayList<RenderQuad> quads, ChunkPos thisPos){
            this.quads = quads;
            this.thisPos = thisPos;
            byteBuffer = MemoryUtil.memAlloc(getQuadBufferSize());
        }
        @Override public void close() {
            MemoryUtil.memFree(byteBuffer);
            if(vertexBuffer != null) vertexBuffer.close();
            if(vertexArray != null) vertexArray.close();
        }
        public void refreshByteBuffer(){
            byteBuffer = MemoryUtil.memRealloc(byteBuffer, getQuadBufferSize());
        }
        public void dataAndBind(Buffer indexBuffer){
            if(vertexBuffer == null) vertexBuffer = new Buffer();
            if(vertexArray == null){
                vertexArray = new VertexArray();
                vertexArray.bind();
                vertexBuffer.bindAsArray();
                program.attrib.attribAndEnable();
                indexBuffer.bindAsElementArray();
                vertexArray.unbind();
            }
            vertexArray.bind();
            vertexBuffer.data(byteBuffer, Constants.BufferMode.DYNAMIC_DRAW);
        }
        public int getQuadBufferSize(){
            //quads.size() * vertexPerQuad * sizePerVertex
            return quads.size() * 4 * 16;
        }
        public boolean shouldUpdate(Vec3d pos){
            boolean result;
            if(lastUpdatePos == null) result = true;
            else {
                double dsx = Math.abs(pos.x - Math.clamp(pos.x, thisPos.x * 16, thisPos.x * 16 + 16));
                double dsz = Math.abs(pos.z - Math.clamp(pos.z, thisPos.z * 16, thisPos.z * 16 + 16));
                boolean resY = Math.abs(pos.y - lastUpdatePos.y) > Math.max(dsx, dsz);
                boolean resX;
                if(pos.x >= thisPos.x * 16 + 16 && lastUpdatePos.x >= thisPos.x * 16 + 16) resX = false;
                else if(pos.x <= thisPos.x * 16 && lastUpdatePos.x <= thisPos.x * 16) resX = false;
                else resX = Math.abs(pos.x - lastUpdatePos.x) > dsz;
                boolean resZ;
                if(pos.z >= thisPos.z * 16 + 16 && lastUpdatePos.z >= thisPos.z * 16 + 16) resZ = false;
                else if(pos.z <= thisPos.z * 16 && lastUpdatePos.z <= thisPos.z * 16) resZ = false;
                else resZ = Math.abs(pos.z - lastUpdatePos.z) > dsx;
                result = resX || resY || resZ;
            }
            if(result) lastUpdatePos = pos;
            return result;
        }
    }
    private final HashMap<ChunkPos, QuadBuffer> vertexBuffers = new HashMap<>();
    private final Buffer indexBuffer = new Buffer();
    private int indexShapeCount = 0;
    private void ensureIndexBufferSize(int shapeCount){
        if(shapeCount <= indexShapeCount) return;
        if(indexShapeCount == 0) indexShapeCount = 1;
        while (indexShapeCount < shapeCount) indexShapeCount *= 2;
        //indexShapeCount * indexCountPerShape * sizeof(int)
        ByteBuffer buffer = MemoryUtil.memAlloc(indexShapeCount * 6 * 4);
        for(int a = 0; a < indexShapeCount; ++a){
            int b = a * 4;
            buffer.putInt(b).putInt(b + 1).putInt(b + 2);
            buffer.putInt(b).putInt(b + 2).putInt(b + 3);
        }
        buffer.flip();
        indexBuffer.data(buffer, Constants.BufferMode.DYNAMIC_DRAW);
    }
    
    private final ArrayList<Pair<ChunkPos, CompletableFuture<QuadBuffer>>> asyncQuadBufferBuilders = new ArrayList<>();
    private void buildQuadBufferAsync(ChunkPos pos, HashMap<ChunkPos, HashMap<BlockPos, MutableInt>> markedPoses, double priority){
        HashMap<BlockPos, MutableInt> current, west, east, north, south;
        current = markedPoses.get(pos);
        if(current == null) return;
        west = markedPoses.get(new ChunkPos(pos.x - 1, pos.z));
        east = markedPoses.get(new ChunkPos(pos.x + 1, pos.z));
        north = markedPoses.get(new ChunkPos(pos.x, pos.z - 1));
        south = markedPoses.get(new ChunkPos(pos.x, pos.z + 1));
        if(west == null || east == null || north == null || south == null) return;
        asyncQuadBufferBuilders.add(new Pair<>(pos, GenericUtils.supplyAsync(()->asyncBuildQuadBuffer(current,
            Sets.union(Sets.union(west.keySet(), east.keySet()), Sets.union(north.keySet(), south.keySet())), pos), priority)));
    }
    //nearBlocks不推荐包含current中的BlockPos
    private static QuadBuffer asyncBuildQuadBuffer(HashMap<BlockPos, MutableInt> current, Set<BlockPos> nearBlocks, ChunkPos thisPos){
        Set<BlockPos> fullBlocks = Sets.union(current.keySet(), nearBlocks);
        ArrayList<RenderQuad> result = new ArrayList<>();
        for(Map.Entry<BlockPos, MutableInt> marked : current.entrySet()){
            for(Direction direction : Direction.values()){
                if(fullBlocks.contains(marked.getKey().offset(direction))) continue;
                result.add(new RenderQuad(marked.getKey(), direction, marked.getValue()));
            }
        }
        return new QuadBuffer(result, thisPos);
    }
    @Override public void reset(ClientWorld world, Vec3d playerEyePos) {
        super.reset(world, playerEyePos);
        resetRender();
    }
    public void resetRender(){
        AlgorithmUtils.cancelTasks(asyncQuadBufferBuilders, Pair::getRight);
        for(QuadBuffer quadBuffer : vertexBuffers.values())
            quadBuffer.close();
        vertexBuffers.clear();
        for(ChunkPos pos : markedPoses.keySet())
            buildQuadBufferAsync(pos, markedPoses, DataUtils.squaredDistance(lastPlayerEyePos, pos));
    }
}
