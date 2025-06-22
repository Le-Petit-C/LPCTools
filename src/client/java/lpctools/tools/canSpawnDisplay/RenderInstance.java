package lpctools.tools.canSpawnDisplay;

import lpctools.generic.GenericUtils;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.gl.*;
import lpctools.util.AlgorithmUtils;
import lpctools.util.MathUtils;
import lpctools.util.javaex.SharedPtr;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

//TODO:渲染范围限制

public class RenderInstance extends DataInstance implements WorldRenderEvents.Last, WorldRenderEvents.DebugRender{
    public RenderInstance(MinecraftClient client, @NotNull BooleanSupplier renderXRays, @NotNull DoubleSupplier renderDistance, @NotNull IRenderMethod renderMethod, @NotNull IntSupplier renderColor) {
        super(client);
        this.renderXRays = renderXRays;
        this.renderDistance = renderDistance;
        this.renderMethod = renderMethod;
        this.renderColor = renderColor;
    }
    public void setRenderMethod(@NotNull IRenderMethod renderMethod) {
        this.renderMethod = renderMethod;
        //TODO...
    }
    @Override protected void registerAll(boolean b){
        super.registerAll(b);
        Registries.WORLD_RENDER_LAST.register(this, b);
        Registries.WORLD_RENDER_BEFORE_DEBUG_RENDER.register(this, b);
    }
    @Override public void close(){
        super.close();
        AlgorithmUtils.cancelTasks(renderBufferBuilders, v->v);
        bufferCache.forEach((key, value)->value.close());
        bufferCache.clear();
        if (sharedBufferData != null) sharedBufferData.releaseNoexcept();
    }
    @Override public void onLast(WorldRenderContext context) {if(renderXRays.getAsBoolean()) render(context);}
    @Override public void beforeDebugRender(WorldRenderContext context) {if(!renderXRays.getAsBoolean()) render(context);}
    @Override protected void onChunkDataLoaded(ChunkPos pos, double distanceSquared) {buildBufferAsync(pos, distanceSquared);}
    @Override public void onStartTick(MinecraftClient mc) {
        super.onStartTick(mc);
        AlgorithmUtils.consumeCompletedTasks(renderBufferBuilders, this::convertAsyncResult);
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
    private void convertAsyncResult(AsyncBuiltResult result){
        RenderBuffer buffer = bufferCache.get(result.data.pos);
        if(buffer != null) buffer.close();
        bufferCache.put(result.data.pos, result.build());
    }
    private final ArrayList<CompletableFuture<AsyncBuiltResult>> renderBufferBuilders = new ArrayList<>();
    private final @NotNull BooleanSupplier renderXRays;
    private final @NotNull DoubleSupplier renderDistance;
    private final @NotNull IntSupplier renderColor;
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
            layer.enableBlend().disableCullFace().enableDepthTest(!renderXRays.getAsBoolean());
            try(MaskLayer furtherLayer = new MaskLayer()){
                furtherLayer.disableDepthWrite();
                for(int a = 0; a < buffersToRender.size(); ++a)
                    buffersToRender.get(a).render(finalMatrixList.get(a), modelMatrixList.get(a), renderColor.getAsInt(), distanceSquared);
            }
            try(MaskLayer furtherLayer = new MaskLayer()){
                furtherLayer.disableColorWrite();
                for(int a = 0; a < buffersToRender.size(); ++a)
                    buffersToRender.get(a).render(finalMatrixList.get(a), modelMatrixList.get(a), renderColor.getAsInt(), distanceSquared);
            }
        }
    }
    private void buildBufferAsync(ChunkPos pos, double distanceSquared){
        if(sharedBufferData == null) sharedBufferData = new SharedPtr<>(SharedBufferData.build(renderMethod)).take();
        AsyncRecordData data = new AsyncRecordData(pos, renderMethod, sharedBufferData);
        ArrayList<BlockPos> poses = canSpawnPoses.get(pos);
        renderBufferBuilders.add(GenericUtils.supplyAsync(()->asyncBufferBuilder(data, poses), distanceSquared));
    }
    private static AsyncBuiltResult asyncBufferBuilder(AsyncRecordData data, ArrayList<BlockPos> poses){
        ByteBuffer result = MemoryUtil.memAlloc(poses.size() * 12);
        poses.forEach(pos->result.putFloat(pos.getX() + 0.5f).putFloat(pos.getY() + 0.5f).putFloat(pos.getZ() + 0.5f));
        return new AsyncBuiltResult(data, result.flip(), poses.size());
    }
    private record AsyncRecordData(ChunkPos pos, IRenderMethod renderMethod, SharedPtr<SharedBufferData> shared){}
    private record AsyncBuiltResult(AsyncRecordData data, ByteBuffer buffer, int instanceCount){
        public RenderBuffer build(){return new RenderBuffer(buffer, instanceCount, data);}
    }
    private static class RenderBuffer implements AutoCloseable{
        private static final RenderProgram program = RenderProgram.program;
        public RenderBuffer(ByteBuffer buffer, int instanceCount, AsyncRecordData data){
            this.renderMethod = data.renderMethod;
            this.instanceCount = instanceCount;
            this.sharedBufferData = data.shared.take();
            SharedBufferData sharedData = data.shared.get();
            instanceBuffer.data(buffer, Constants.BufferMode.STATIC_DRAW);
            MemoryUtil.memFree(buffer);
            vertexArray.bind();
            program.attribAndEnable(instanceBuffer, sharedData.vertexBuffer);
            sharedData.indexBuffer.bindAsElementArray();
        }
        public void render(Matrix4f finalMatrix, Matrix4f modelMatrix, int color, double distanceSquared){
            vertexArray.bind();
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
