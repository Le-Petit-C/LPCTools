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
import org.joml.Vector3i;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
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
        bufferCache.forEach((key, value)->value.forEach((k, v)->v.close()));
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
        HashMap<Vector3i, RenderBuffer> map = bufferCache.get(result.data.pos);
        if(map != null) map.values().forEach(RenderBuffer::close);
        bufferCache.put(result.data.pos, result.buildRenderBuffer());
    }
    private final ArrayList<CompletableFuture<AsyncBuiltResult>> renderBufferBuilders = new ArrayList<>();
    private final @NotNull BooleanSupplier renderXRays;
    private final @NotNull DoubleSupplier renderDistance;
    private final @NotNull IntSupplier renderColor;
    private @NotNull IRenderMethod renderMethod;
    private final HashMap<ChunkPos, HashMap<Vector3i, RenderBuffer>> bufferCache = new HashMap<>();
    private void render(WorldRenderContext context){
        double distanceSquared = MathUtils.square(renderDistance.getAsDouble());
        double distanceLimit = MathUtils.square(renderDistance.getAsDouble() + Math.sqrt(3) * 8);
        Vec3d camPos = context.camera().getPos();
        Iterable<Chunk> poses = AlgorithmUtils.iterateLoadedChunksFromClosest(context.world(), camPos);
        try(MaskLayer layer = new MaskLayer()){
            layer.enableBlend().disableCullFace().enableDepthTest(!renderXRays.getAsBoolean());
            for(Chunk chunk : poses){
                ChunkPos pos = chunk.getPos();
                if(MathUtils.squaredDistance(camPos, pos) >= distanceLimit) continue;
                HashMap<Vector3i, RenderBuffer> buffers = bufferCache.get(pos);
                if(buffers == null) continue;
                Matrix4f modelMatrix = MathUtils.inverseOffsetMatrix4f(camPos.subtract(pos.x * 16, 0, pos.z * 16).toVector3f());
                context.positionMatrix().mul(modelMatrix, modelMatrix);
                Matrix4f finalMatrix = context.projectionMatrix().mul(modelMatrix, new Matrix4f());
                buffers.forEach((key, value)->{
                    if(camPos.squaredDistanceTo(key.x * 16.0 + 8.0, key.y * 16.0 + 8.0, key.z * 16.0 + 8.0) >= distanceLimit) return;
                    value.render(finalMatrix, modelMatrix, renderColor.getAsInt(), distanceSquared);
                });
            }
        }
    }
    private void buildBufferAsync(ChunkPos pos, double distanceSquared){
        if(sharedBufferData == null) sharedBufferData = new SharedPtr<>(SharedBufferData.build(renderMethod)).take();
        AsyncRecordData data = new AsyncRecordData(pos, renderMethod, sharedBufferData);
        ArrayList<BlockPos> poses = canSpawnPoses.get(pos);
        renderBufferBuilders.add(GenericUtils.supplyAsync(()->asyncBufferBuilder(data, poses), distanceSquared));
    }
    private static AsyncBuiltResult asyncBufferBuilder(AsyncRecordData data, ArrayList<BlockPos> canSpawnPoses){
        HashMap<Vector3i, ArrayList<BlockPos>> separated = new HashMap<>();
        Function<Vector3i, ArrayList<BlockPos>> allocator = k->new ArrayList<>();
        canSpawnPoses.forEach(p->separated.computeIfAbsent(MathUtils.getSubChunkPos(p).add(data.pos.x, 0, data.pos.z), allocator).add(p.toImmutable()));
        AsyncBuiltResult result = new AsyncBuiltResult(data, new HashMap<>());
        separated.forEach((key, value)->result.buffer.put(key, asyncBufferBuilder(value)));
        return result;
    }
    private static AsyncBuiltBuffer asyncBufferBuilder(ArrayList<BlockPos> poses){
        ByteBuffer result = MemoryUtil.memAlloc(poses.size() * 12);
        poses.forEach(pos->result.putFloat(pos.getX() + 0.5f).putFloat(pos.getY() + 0.5f).putFloat(pos.getZ() + 0.5f));
        return new AsyncBuiltBuffer(result.flip(), poses.size());
    }
    private record AsyncRecordData(ChunkPos pos, IRenderMethod renderMethod, SharedPtr<SharedBufferData> shared){}
    private record AsyncBuiltBuffer(ByteBuffer buffer, int instanceCount){}
    private record AsyncBuiltResult(AsyncRecordData data, HashMap<Vector3i, AsyncBuiltBuffer> buffer){
        public HashMap<Vector3i, RenderBuffer> buildRenderBuffer(){
            HashMap<Vector3i, RenderBuffer> res = new HashMap<>();
            buffer.forEach((key, value)->res.put(key, new RenderBuffer(value.buffer, value.instanceCount, data)));
            VertexArray.unbindStatic();
            return res;
        }
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
