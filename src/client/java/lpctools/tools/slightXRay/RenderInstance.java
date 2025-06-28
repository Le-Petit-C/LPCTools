package lpctools.tools.slightXRay;

import com.google.common.collect.Sets;
import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.gl.Buffer;
import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.lpcfymasaapi.gl.MaskLayer;
import lpctools.lpcfymasaapi.gl.VertexArray;
import lpctools.shader.ShaderPrograms;
import lpctools.util.AlgorithmUtils;
import lpctools.util.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CompletableFuture;

//TODO:不使用形状索引而是合并重合顶点的索引（比如相邻矩形）以节省空间

public class RenderInstance extends DataInstance implements WorldRenderEvents.End, WorldRenderEvents.Start {
    public final SlightXRay parent;
    RenderInstance(SlightXRay parent, MinecraftClient client){
        super(client);
        this.parent = parent;
        Registries.WORLD_RENDER_END.register(this);
        Registries.WORLD_RENDER_START.register(this);
        shapeList = parent.displayRange.buildShapeList();
    }
    @Override public void close(){
        Registries.WORLD_RENDER_START.unregister(this);
        Registries.WORLD_RENDER_END.unregister(this);
        for(QuadBuffer buffer : vertexBuffers.values())
            buffer.close();
        vertexBuffers.clear();
        super.close();
    }
    public void onRenderRangeChanged(RangeLimitConfig rangeLimit){
        ShapeList newList = rangeLimit.buildShapeList();
        if(newList.equals(shapeList)) return;
        shapeList = newList;
        resetRender();
    }
    @Override protected void onXRayChunkUpdated(ChunkPos pos, double distanceSquare) {
        HashMap<BlockPos, MutableInt> poses = super.markedPoses.get(pos);
        if(poses == null){
            AlgorithmUtils.closeNoExcept(vertexBuffers.remove(pos));
            return;
        }
        buildQuadBufferAsync(pos, super.markedPoses, distanceSquare, shapeList);
        buildQuadBufferAsync(new ChunkPos(pos.x - 1, pos.z), super.markedPoses, distanceSquare, shapeList);
        buildQuadBufferAsync(new ChunkPos(pos.x + 1, pos.z), super.markedPoses, distanceSquare, shapeList);
        buildQuadBufferAsync(new ChunkPos(pos.x, pos.z - 1), super.markedPoses, distanceSquare, shapeList);
        buildQuadBufferAsync(new ChunkPos(pos.x, pos.z + 1), super.markedPoses, distanceSquare, shapeList);
    }
    
    private MutableInt getWithShapeList(BlockPos pos){
        if(!shapeList.testPos(pos)) return null;
        else return get(pos);
    }
    private void removePos(BlockPos pos){
        for(Direction direction : Direction.values()){
            BlockPos nextPos = pos.offset(direction);
            MutableInt nextColor = getWithShapeList(nextPos);
            if(nextColor == null) continue;
            QuadBuffer buffer = vertexBuffers.get(new ChunkPos(nextPos));
            if(buffer == null) continue;
            buffer.quads.add(new RenderQuad(nextPos.toImmutable(), direction.getOpposite(), nextColor));
            buffer.refreshByteBuffer();
            buffer.markUnsorted();
        }
        QuadBuffer currentBuffer = vertexBuffers.get(new ChunkPos(pos));
        if(currentBuffer == null) return;
        ArrayList<RenderQuad> quads = currentBuffer.quads;
        AlgorithmUtils.fastRemove(quads, v->v.attachedBlock.equals(pos));
        currentBuffer.refreshByteBuffer();
        currentBuffer.markUnsorted();
    }
    private void addPos(BlockPos pos, MutableInt currentColor){
        QuadBuffer currentBuffer = vertexBuffers.get(new ChunkPos(pos));
        ArrayList<RenderQuad> quads = currentBuffer != null ? currentBuffer.quads : null;
        if(quads != null) AlgorithmUtils.fastRemove(quads, v->v.attachedBlock.equals(pos));
        for(Direction direction : Direction.values()){
            BlockPos nextPos = pos.offset(direction);
            MutableInt nextColor = getWithShapeList(nextPos);
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
            buffer.markUnsorted();
        }
        if(currentBuffer == null)  return;
        currentBuffer.refreshByteBuffer();
        currentBuffer.markUnsorted();
    }
    
    @Override public void onClientWorldChunkSetBlockState(WorldChunk chunk, BlockPos pos, BlockState lastState, BlockState newState) {
        HashMap<BlockPos, MutableInt> lastContained = new HashMap<>();
        for(BlockPos pos1 : AlgorithmUtils.iterateInManhattanDistance(pos, 2)){
            MutableInt color = getWithShapeList(pos1);
            if(color != null) lastContained.put(pos1.toImmutable(), color);
        }
        super.onClientWorldChunkSetBlockState(chunk, pos, lastState, newState);
        for(BlockPos pos1 : AlgorithmUtils.iterateInManhattanDistance(pos, 2)){
            MutableInt currentColor = getWithShapeList(pos1);
            if(Objects.equals(lastContained.get(pos1), currentColor)) continue;
            if(currentColor == null) removePos(pos1);
            else addPos(pos1, currentColor);
            QuadBuffer quadBuffer = vertexBuffers.get(new ChunkPos(pos1));
            if(quadBuffer != null) quadBuffer.markUnsorted();
        }
    }
    @Override public void afterWorldChange(MinecraftClient mc, ClientWorld world) {
        super.afterWorldChange(mc, world);
        clearRender();
    }
    @Override public void onStartTick(MinecraftClient mc){
        super.onStartTick(mc);
        AlgorithmUtils.consumeCompletedTasks(asyncQuadBufferBuilders, (pos, buffer)->AlgorithmUtils.closeNoExcept(vertexBuffers.put(pos, buffer)));
    }
    private void sort(ArrayList<RenderQuad> quads, Vec3d cam){
        quads.sort(Comparator.<RenderQuad>comparingDouble(quad -> quad.centerPos.squaredDistanceTo(cam)).reversed());
    }
    private ChunkRenderPrepareResult asyncPrepareRenderChunk(QuadBuffer quadBuffer, Vec3d camPos, Matrix4f projection_view_matrix){
        sort(quadBuffer.quads, camPos);
        int offX = -quadBuffer.thisPos.x << 4, offZ = -quadBuffer.thisPos.z << 4;
        for(RenderQuad quad : quadBuffer.quads)
            quad.vertex(quadBuffer.byteBuffer, offX, offZ);
        quadBuffer.byteBuffer.flip();
        return new ChunkRenderPrepareResult(quadBuffer.quads.size(), ()->quadBuffer.dataAndBind(indexBuffer),
            camPos, quadBuffer.thisPos, projection_view_matrix);
    }
    private RenderPrepareResult asyncPrepareRenderMain(World world, Vec3d camPos, Matrix4f projection_view_matrix){
        int maxShapeCount = 0;
        ArrayList<CompletableFuture<ChunkRenderPrepareResult>> renderBufferBuilders = new ArrayList<>();
        for(Chunk chunk : AlgorithmUtils.iterateLoadedChunksFromClosest(world, camPos)){
            ChunkPos pos = chunk.getPos();
            QuadBuffer buffer = vertexBuffers.get(pos);
            if(buffer == null || buffer.quads.isEmpty()) continue;
            if(buffer.quads.size() > maxShapeCount) maxShapeCount = buffer.quads.size();
            if(buffer.shouldUpdate(camPos) || buffer.vertexArray == null)
                renderBufferBuilders.add(CompletableFuture.supplyAsync(()->asyncPrepareRenderChunk(buffer, camPos, projection_view_matrix)));
            else renderBufferBuilders.add(CompletableFuture.completedFuture(new ChunkRenderPrepareResult(buffer.quads.size(), ()->buffer.vertexArray.bind(),
                camPos, pos, projection_view_matrix)));
        }
        return new RenderPrepareResult(renderBufferBuilders, maxShapeCount);
    }
    private record ChunkRenderPrepareResult(int shapeCount, Matrix4f finalMatrix, Runnable bindOperation){
        ChunkRenderPrepareResult(int size, Runnable bindOperation, Vec3d camPos, ChunkPos pos, Matrix4f projection_view_matrix){
            this(size, getFinalMatrix(camPos, pos, projection_view_matrix), bindOperation);
        }
        private static Matrix4f getFinalMatrix(Vec3d camPos, ChunkPos pos, Matrix4f projection_view_matrix){
            Matrix4f finalMatrix = MathUtils.inverseOffsetMatrix4f(camPos.subtract(pos.x << 4, 0, pos.z << 4).toVector3f());
            return projection_view_matrix.mul(finalMatrix, finalMatrix);
        }
    }
    private record RenderPrepareResult(ArrayList<CompletableFuture<ChunkRenderPrepareResult>> futures, int maxShapeCount){}
    //返回值是最大形状数量
    CompletableFuture<RenderPrepareResult> renderTask;
    @Override public void onStart(WorldRenderContext context){
        Vec3d camPos = context.camera().getPos();
        Matrix4f matrix = new Matrix4f(context.matrixStack().peek().getPositionMatrix());
        context.projectionMatrix().mul(matrix, matrix);
        World world = context.world();
        renderTask = CompletableFuture.supplyAsync(()->asyncPrepareRenderMain(world, camPos, matrix));
    }
    @Override public void onEnd(WorldRenderContext context) {
        RenderPrepareResult result = renderTask.join();
        ensureIndexBufferSize(result.maxShapeCount);
        try(MaskLayer layer = new MaskLayer()){
            layer.enableBlend().enableCullFace(parent.useCullFace.getBooleanValue()).disableDepthTest();
            ShaderPrograms.PositionColorProgram program = ShaderPrograms.POSITION_COLOR_PROGRAM;
            for(CompletableFuture<ChunkRenderPrepareResult> future : result.futures){
                ChunkRenderPrepareResult chunkResult = future.join();
                chunkResult.bindOperation.run();
                program.setFinalMatrix(chunkResult.finalMatrix);
                program.useAndUniform();
                Constants.DrawMode.TRIANGLES.drawElements(chunkResult.shapeCount * 6, Constants.IndexType.INT);
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
        public void refreshByteBuffer(){byteBuffer = MemoryUtil.memRealloc(byteBuffer, getQuadBufferSize());}
        public void markUnsorted(){lastUpdatePos = null;}
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
    
    private final HashMap<ChunkPos, CompletableFuture<QuadBuffer>> asyncQuadBufferBuilders = new HashMap<>();
    private void buildQuadBufferAsync(ChunkPos pos, HashMap<ChunkPos, HashMap<BlockPos, MutableInt>> markedPoses, double priority, ShapeList shapeList){
        HashMap<BlockPos, MutableInt> current, west, east, north, south;
        current = markedPoses.get(pos);
        if(current == null) return;
        west = markedPoses.get(new ChunkPos(pos.x - 1, pos.z));
        east = markedPoses.get(new ChunkPos(pos.x + 1, pos.z));
        north = markedPoses.get(new ChunkPos(pos.x, pos.z - 1));
        south = markedPoses.get(new ChunkPos(pos.x, pos.z + 1));
        if(west == null || east == null || north == null || south == null) return;
        AlgorithmUtils.cancelTask(asyncQuadBufferBuilders.remove(pos));
        asyncQuadBufferBuilders.put(pos, GenericUtils.supplyAsync(()->asyncBuildQuadBuffer(current,
            Sets.union(Sets.union(west.keySet(), east.keySet()), Sets.union(north.keySet(), south.keySet())), pos, shapeList), priority));
    }
    //nearBlocks不推荐包含current中的BlockPos
    private static QuadBuffer asyncBuildQuadBuffer(HashMap<BlockPos, MutableInt> current, Set<BlockPos> nearBlocks, ChunkPos thisPos, ShapeList shapeList){
        Set<BlockPos> fullBlocks = Sets.union(current.keySet(), nearBlocks);
        ArrayList<RenderQuad> result = new ArrayList<>();
        for(Map.Entry<BlockPos, MutableInt> marked : current.entrySet()){
            if(!shapeList.testPos(marked.getKey())) continue;
            for(Direction direction : Direction.values()){
                BlockPos offset = marked.getKey().offset(direction);
                if(shapeList.testPos(offset) && fullBlocks.contains(offset)) continue;
                result.add(new RenderQuad(marked.getKey(), direction, marked.getValue()));
            }
        }
        return new QuadBuffer(result, thisPos);
    }
    @Override public void resetData() {
        super.resetData();
        resetRender();
    }
    public void clearRender(){
        AlgorithmUtils.cancelTasks(asyncQuadBufferBuilders.values());
        AlgorithmUtils.closeNoExcept(vertexBuffers.values());
    }
    public void resetRender(){
        if(client.player == null) return;
        Vec3d playerEyePos = client.player.getEyePos();
        for(ChunkPos pos : markedPoses.keySet())
            buildQuadBufferAsync(pos, markedPoses, MathUtils.squaredDistance(playerEyePos, pos), shapeList);
    }
    private @NotNull ShapeList shapeList;
}
