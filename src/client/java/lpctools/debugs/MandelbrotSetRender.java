package lpctools.debugs;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.util.CachedSupplier;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static net.minecraft.client.gl.RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET;

public class MandelbrotSetRender extends BooleanThirdListConfig implements Registries.WorldLastRender {
    public final IntegerConfig maxDepth;
    public final DoubleConfig stretch;
    public final CachedSupplier<RenderSources> renderSources = new CachedSupplier<>(RenderSources::new);
    public class RenderSources implements AutoCloseable {
        public static final RenderPipeline mandelbrotSetPipeline
            = RenderPipeline.builder(TRANSFORMS_AND_PROJECTION_SNIPPET)
            .withUniform("Mandelbrot", UniformType.UNIFORM_BUFFER)
            .withVertexShader("core/position_tex")
            .withFragmentShader(Identifier.of("lpctools", "core/mandelbrot_set"))
            .withVertexFormat(VertexFormats.POSITION_TEXTURE, VertexFormat.DrawMode.QUADS)
            .withLocation(Identifier.of("lpctools", "pipeline/mandelbrot"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
        public static final RenderSystem.ShapeIndexBuffer mandelbrotSetShapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        private final GpuBuffer mandelbrotUniformBuffer = RenderSystem.getDevice()
            .createBuffer(() -> "Mandelbrot Uniform",
                GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 48);
        private int lastMaxDepth = -1;
        private final GpuBuffer vertexBuffer = RenderSystem.getDevice()
            .createBuffer(() -> "Mandelbrot Vertex Buffer",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, (long)VertexFormats.POSITION_TEXTURE.getVertexSize() * Float.BYTES);
        private float lastStretch = Float.NaN;
        public GpuBuffer getUpdatedMandelbrotUniformBuffer(){
            int maxDepth = MandelbrotSetRender.this.maxDepth.getAsInt();
            boolean needUpdate = lastMaxDepth != maxDepth;
            if(needUpdate){
                var encoder = RenderSystem.getDevice().createCommandEncoder();
                ByteBuffer buffer = MemoryUtil.memAlloc(48);
                buffer.putFloat(0.0f).putFloat(0.0f).putFloat(0.0f).putFloat(1.0f); // setColor
                buffer.putFloat(1.0f).putFloat(1.0f).putFloat(1.0f).putFloat(1.0f); // outColor
                buffer.putInt(lastMaxDepth = maxDepth); // maxDepth
                buffer.put(new byte[12]); // padding
                buffer.flip();
                encoder.writeToBuffer(mandelbrotUniformBuffer.slice(), buffer);
                MemoryUtil.memFree(buffer);
            }
            return mandelbrotUniformBuffer;
        }
        public GpuBuffer getUpdatedVertexBuffer(){
            float stretch = (float)MandelbrotSetRender.this.stretch.getAsDouble();
            boolean needUpdate = lastStretch != stretch;
            if(needUpdate){
                var encoder = RenderSystem.getDevice().createCommandEncoder();
                ByteBuffer buffer = MemoryUtil.memAlloc(VertexFormats.POSITION_TEXTURE.getVertexSize() * Float.BYTES);
                // 四个顶点，覆盖整个屏幕
                float sp = stretch * 2.0f;
                buffer.putFloat(-sp).putFloat(0.0f).putFloat(-sp).putFloat(-2.0f).putFloat(-2.0f); // north-west
                buffer.putFloat( sp).putFloat(0.0f).putFloat(-sp).putFloat( 2.0f).putFloat(-2.0f); // north-east
                buffer.putFloat( sp).putFloat(0.0f).putFloat( sp).putFloat( 2.0f).putFloat( 2.0f); // south-east
                buffer.putFloat(-sp).putFloat(0.0f).putFloat( sp).putFloat(-2.0f).putFloat( 2.0f); // south-west
                buffer.flip();
                encoder.writeToBuffer(vertexBuffer.slice(), buffer);
                MemoryUtil.memFree(buffer);
                lastStretch = stretch;
            }
            return vertexBuffer;
        }
        
        @Override public void close() {
            mandelbrotUniformBuffer.close();
            vertexBuffer.close();
        }
    }
    public MandelbrotSetRender(ILPCConfigReadable parent) {
        super(parent, "mandelbrotSet", false, null);
        try(ConfigListLayer ignored = new ConfigListLayer(this)){
            maxDepth = addIntegerConfig("maxDepth", 128, 0, 65536);
            stretch = addDoubleConfig("stretch", 1);
        }
    }
    
    @Override public void onValueChanged() {
        super.onValueChanged();
        Registries.MASA_WORLD_RENDER_LAST.register(this, getBooleanValue());
    }
    
    @Override public void onLast(Registries.MASAWorldRenderContext context) {
        var fb = context.fb();
        GpuTextureView colorAttachmentView = fb.getColorAttachmentView();
        GpuTextureView depthAttachmentView = fb.useDepthAttachment ? fb.getDepthAttachmentView() : null;
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
            .write(RenderSystem.getModelViewMatrix().translate(context.camera().getCameraPos().toVector3f().mul(-1),
                new Matrix4f()), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
        GpuBufferSlice projection = RenderSystem.getProjectionMatrixBuffer();
        var renderSources = this.renderSources.get();
        GpuBuffer mandelbrotUniformBuffer = renderSources.getUpdatedMandelbrotUniformBuffer();
        GpuBuffer vertexBuffer = renderSources.getUpdatedVertexBuffer();
        RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSources.mandelbrotSetShapeIndexBuffer;
        GpuBuffer indexBuffer = shapeIndexBuffer.getIndexBuffer(6);
        VertexFormat.IndexType indexType = shapeIndexBuffer.getIndexType();
        try(RenderPass renderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "LPCTools Mandelbrot Set",
                colorAttachmentView, OptionalInt.empty(), depthAttachmentView, OptionalDouble.empty())){
            renderPass.setPipeline(RenderSources.mandelbrotSetPipeline);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("Projection", projection);
            renderPass.setUniform("Mandelbrot", mandelbrotUniformBuffer);
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.setIndexBuffer(indexBuffer, indexType);
            renderPass.drawIndexed(0, 0, 6, 1);
        }
    }
}
