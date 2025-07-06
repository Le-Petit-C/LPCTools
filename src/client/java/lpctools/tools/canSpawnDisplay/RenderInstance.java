package lpctools.tools.canSpawnDisplay;

import lpctools.compact.derived.ShapeList;
import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.gl.*;
import lpctools.lpcfymasaapi.gl.furtherWarpped.BlendPresets;
import lpctools.util.AlgorithmUtils;
import lpctools.util.DataUtils;
import lpctools.util.MathUtils;
import lpctools.util.javaex.SharedPtr;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static lpctools.tools.canSpawnDisplay.CanSpawnDisplay.*;

public class RenderInstance extends DataInstance implements WorldRenderEvents.Last, WorldRenderEvents.DebugRender{
    public RenderInstance(MinecraftClient client) {
        super(client);
        renderMethod = CanSpawnDisplay.renderMethod.get();
        shapeList = rangeLimit.buildShapeList();
    }
    public void setRenderMethod(@NotNull IRenderMethod renderMethod) {
        this.renderMethod = renderMethod;
        if (sharedBufferData != null) {
            sharedBufferData.releaseNoexcept();
            sharedBufferData = null;
        }
        clearRenderBuffer();
        resetRender();
    }
    public void resetRender(){
        if(client.player == null) return;
        for(ChunkPos pos : canSpawnPoses.keySet())
            buildBufferAsync(pos, MathUtils.squaredDistance(client.player.getEyePos(), pos));
    }
    public void clearRenderBuffer(){
        AlgorithmUtils.cancelTasks(renderBufferBuilders.values(), v->v);
        bufferCache.forEach((key, value)->value.close());
        bufferCache.clear();
        if (sharedBufferData != null) sharedBufferData.releaseNoexcept();
        sharedBufferData = null;
    }
    public void onRenderRangeChanged(RangeLimitConfig rangeLimit){
        ShapeList newList = rangeLimit.buildShapeList();
        if(newList.equals(shapeList)) return;
        shapeList = newList;
        resetRender();
    }
    @Override protected void registerAll(boolean b){
        super.registerAll(b);
        Registries.WORLD_RENDER_LAST.register(this, b);
        Registries.WORLD_RENDER_BEFORE_DEBUG_RENDER.register(this, b);
    }
    @Override public void close(){
        super.close();
        clearRenderBuffer();
    }
    @Override public void onChunkUnload(ClientWorld world, WorldChunk chunk) {
        super.onChunkUnload(world, chunk);
        ChunkPos pos = chunk.getPos();
        AlgorithmUtils.cancelTask(renderBufferBuilders.remove(pos));
        AlgorithmUtils.closeNoExcept(bufferCache.remove(pos));
    }
    
    @Override public void onLast(WorldRenderContext context) {if(renderXRays.getAsBoolean()) render(context);}
    @Override public void beforeDebugRender(WorldRenderContext context) {if(!renderXRays.getAsBoolean()) render(context);}
    @Override protected void onChunkDataLoaded(ChunkPos pos, double distanceSquared) {buildBufferAsync(pos, distanceSquared);}
    @Override public void onStartTick(MinecraftClient mc) {
        super.onStartTick(mc);
        try(MaskLayer layer = new MaskLayer()){
            AlgorithmUtils.consumeCompletedTasks(renderBufferBuilders, (pos, result)->convertAsyncResult(pos, result, layer));
        }
    }
    @Nullable SharedPtr<SharedBufferData> sharedBufferData;
    
    private record SharedBufferData(Buffer vertexBuffer, Buffer indexBuffer) implements AutoCloseable{
        @Override public void close() {
            vertexBuffer.close();
            indexBuffer.close();
        }
        public static SharedBufferData build(IRenderMethod renderMethod){
            ByteBuffer vertexByteBuffer = MemoryUtil.memAlloc(renderMethod.getVertexBufferSize());
            ByteBuffer indexByteBuffer = MemoryUtil.memAlloc(renderMethod.getIndexCount());
            renderMethod.vertex(vertexByteBuffer, indexByteBuffer);
            Buffer vertexBuffer = new Buffer().data(vertexByteBuffer.flip(), Constants.BufferMode.STATIC_DRAW);
            Buffer indexBuffer = new Buffer().data(indexByteBuffer.flip(), Constants.BufferMode.STATIC_DRAW);
            return new SharedBufferData(vertexBuffer, indexBuffer);
        }
    }
    private void convertAsyncResult(ChunkPos pos, AsyncBuiltResult result, MaskLayer layer){
        AlgorithmUtils.closeNoExcept(bufferCache.remove(pos));
        //if(result.buffer.remaining() == 0) return;
        bufferCache.put(pos, result.build(layer));
    }
    private @NotNull ShapeList shapeList;
    private final HashMap<ChunkPos, CompletableFuture<AsyncBuiltResult>> renderBufferBuilders = new HashMap<>();
    private @NotNull IRenderMethod renderMethod;
    private final HashMap<ChunkPos, RenderBuffer> bufferCache = new HashMap<>();
    private void render(WorldRenderContext context){
        double distanceSquared = MathUtils.square(renderDistance.getAsDouble());
        double distanceLimit = MathUtils.square(renderDistance.getAsDouble() + Math.sqrt(2) * 8);
        Vec3d camPos = context.camera().getPos();
        Iterable<Chunk> poses = AlgorithmUtils.iterateLoadedChunksFromClosest(context.world(), camPos);
        ArrayList<RenderBuffer> buffersToRender = new ArrayList<>();
        ArrayList<Matrix4f> modelMatrixList = new ArrayList<>();
        ArrayList<Matrix4f> finalMatrixList = new ArrayList<>();
        for(Chunk chunk : poses){
            ChunkPos pos = chunk.getPos();
            if(MathUtils.squaredDistance(camPos, pos) >= distanceLimit) continue;
            RenderBuffer buffer = bufferCache.get(pos);
            if(buffer == null) continue;
            Matrix4f modelMatrix = MathUtils.inverseOffsetMatrix4f(camPos.subtract(pos.x * 16, 0, pos.z * 16).toVector3f());
            context.positionMatrix().mul(modelMatrix, modelMatrix);
            Matrix4f finalMatrix = context.projectionMatrix().mul(modelMatrix, new Matrix4f());
            buffersToRender.add(buffer);
            modelMatrixList.add(modelMatrix);
            finalMatrixList.add(finalMatrix);
        }
        try(MaskLayer layer = new MaskLayer()){
            int color = DataUtils.argb2agbr(displayColor.getIntegerValue());
            boolean hasAlpha = (color >>> 24) != 0xff;
            layer.enableBlend(hasAlpha).disableCullFace().enableDepthTest(!renderXRays.getAsBoolean());
            layer.blendState(BlendPresets.STANDARD_ALPHA);
            if(renderXRays.getAsBoolean() || !hasAlpha){
                for(int a = 0; a < buffersToRender.size(); ++a)
                    buffersToRender.get(a).render(finalMatrixList.get(a), modelMatrixList.get(a), color, distanceSquared, layer);
            }
            else{
                layer.disableDepthWrite();
                for(int a = 0; a < buffersToRender.size(); ++a)
                    buffersToRender.get(a).render(finalMatrixList.get(a), modelMatrixList.get(a), color, distanceSquared, layer);
                layer.pop();
                layer.disableColorWrite();
                for(int a = 0; a < buffersToRender.size(); ++a)
                    buffersToRender.get(a).render(finalMatrixList.get(a), modelMatrixList.get(a), color, distanceSquared, layer);
                layer.pop();
            }
        }
    }
    private void buildBufferAsync(ChunkPos pos, double distanceSquared){
        AlgorithmUtils.cancelTask(renderBufferBuilders.remove(pos));
        if(sharedBufferData == null) sharedBufferData = new SharedPtr<>(SharedBufferData.build(renderMethod)).take();
        AsyncRecordData data = new AsyncRecordData(renderMethod, sharedBufferData, pos);
        ArrayList<BlockPos> poses = canSpawnPoses.get(pos);
        ShapeList finalShapeList = shapeList;
        renderBufferBuilders.put(pos, GenericUtils.supplyAsync(()->asyncBufferBuilder(data, poses, finalShapeList), distanceSquared));
    }
    private static AsyncBuiltResult asyncBufferBuilder(AsyncRecordData data, ArrayList<BlockPos> poses, ShapeList shapeList){
        ByteBuffer result = MemoryUtil.memAlloc(poses.size() * 12);
        BlockPos.Mutable _pos = new BlockPos.Mutable();
        int x = data.pos.x << 4, z = data.pos.z << 4;
        MutableInt count = new MutableInt(0);
        poses.forEach(pos->{
            _pos.set(pos.getX() + x, pos.getY(), pos.getZ() + z);
            if(!shapeList.testPos(_pos)) return;
            result.putFloat(pos.getX() + 0.5f).putFloat(pos.getY() + 0.5f).putFloat(pos.getZ() + 0.5f);
            count.add(1);
        });
        return new AsyncBuiltResult(data, result.flip(), count.intValue());
    }
    private record AsyncRecordData(IRenderMethod renderMethod, SharedPtr<SharedBufferData> shared, ChunkPos pos){}
    private record AsyncBuiltResult(AsyncRecordData data, ByteBuffer buffer, int instanceCount){
        public RenderBuffer build(MaskLayer layer){return new RenderBuffer(buffer, instanceCount, data, layer);}
    }
    private static class RenderBuffer implements AutoCloseable{
        private static final RenderProgram program = RenderProgram.program;
        public RenderBuffer(ByteBuffer buffer, int instanceCount, AsyncRecordData data, MaskLayer layer){
            this.renderMethod = data.renderMethod;
            this.instanceCount = instanceCount;
            this.sharedBufferData = data.shared.take();
            SharedBufferData sharedData = data.shared.get();
            instanceBuffer.data(buffer, Constants.BufferMode.STATIC_DRAW);
            MemoryUtil.memFree(buffer);
            layer.bindArray(vertexArray);
            program.attribAndEnable(instanceBuffer, sharedData.vertexBuffer);
            sharedData.indexBuffer.bindAsElementArray();
        }
        public void render(Matrix4f finalMatrix, Matrix4f modelMatrix, int color, double distanceSquared, MaskLayer layer){
            layer.bindArray(vertexArray);
            program.setUniformCache(finalMatrix, modelMatrix, color, distanceSquared);
            program.useAndUniform();
            renderMethod.getDrawMode().drawElementsInstanced(renderMethod.getIndexCount(), Constants.IndexType.BYTE, instanceCount);
        }
        @Override public void close(){
            instanceBuffer.close();
            vertexArray.close();
            sharedBufferData.releaseNoexcept();
        }
        private final SharedPtr<SharedBufferData> sharedBufferData;
        private final VertexArray vertexArray = new VertexArray();
        private final Buffer instanceBuffer = new Buffer();
        private final IRenderMethod renderMethod;
        private final int instanceCount;
    }
}
