package lpctools.debugs;

import com.mojang.blaze3d.buffers.BufferType;
import com.mojang.blaze3d.buffers.BufferUsage;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import lpctools.lpcfymasaapi.LPCConfigStatics.ConfigListLayer;
import lpctools.lpcfymasaapi.Registries;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.util.CachedSupplier;
import net.minecraft.resources.ResourceLocation;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static net.minecraft.client.renderer.RenderPipelines.MATRICES_SNIPPET;

public class MandelbrotSetRender extends BooleanThirdListConfig implements Registries.WorldLastRender {
    public final IntegerConfig maxDepth;
    public final DoubleConfig stretch;
    public final CachedSupplier<RenderSources> renderSources = new CachedSupplier<>(RenderSources::new);
    public class RenderSources implements AutoCloseable {
        public static final RenderPipeline mandelbrotSetPipeline
            = RenderPipeline.builder(MATRICES_SNIPPET)
            .withUniform("setColor", UniformType.VEC4)
            .withUniform("outColor", UniformType.VEC4)
            .withUniform("maxDepth", UniformType.INT)
            .withVertexShader("core/position_tex")
            .withFragmentShader(ResourceLocation.fromNamespaceAndPath("lpctools", "core/mandelbrot_set"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.DrawMode.QUADS)
            .withLocation(ResourceLocation.fromNamespaceAndPath("lpctools", "pipeline/mandelbrot"))
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
        public static final RenderSystem.AutoStorageIndexBuffer mandelbrotSetShapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
        private final GpuBuffer vertexBuffer = RenderSystem.getDevice()
            .createBuffer(() -> "Mandelbrot Vertex Buffer", BufferType.VERTICES, BufferUsage.STATIC_WRITE, DefaultVertexFormat.POSITION_TEX.getVertexSize() * Float.BYTES);
        private float lastStretch = Float.NaN;
        public GpuBuffer getUpdatedVertexBuffer(){
            float stretch = (float)MandelbrotSetRender.this.stretch.getAsDouble();
            boolean needUpdate = lastStretch != stretch;
            if(needUpdate){
                var encoder = RenderSystem.getDevice().createCommandEncoder();
                ByteBuffer buffer = MemoryUtil.memAlloc(DefaultVertexFormat.POSITION_TEX.getVertexSize() * Float.BYTES);
                // 四个顶点，覆盖整个屏幕
                float sp = stretch * 2.0f;
                buffer.putFloat(-sp).putFloat(0.0f).putFloat(-sp).putFloat(-2.0f).putFloat(-2.0f); // north-west
                buffer.putFloat( sp).putFloat(0.0f).putFloat(-sp).putFloat( 2.0f).putFloat(-2.0f); // north-east
                buffer.putFloat( sp).putFloat(0.0f).putFloat( sp).putFloat( 2.0f).putFloat( 2.0f); // south-east
                buffer.putFloat(-sp).putFloat(0.0f).putFloat( sp).putFloat(-2.0f).putFloat( 2.0f); // south-west
                buffer.flip();
                encoder.writeToBuffer(vertexBuffer, buffer, 0);
                MemoryUtil.memFree(buffer);
                lastStretch = stretch;
            }
            return vertexBuffer;
        }
        
        @Override public void close() {
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
        GpuTexture colorAttachmentView = fb.getColorTexture();
        GpuTexture depthAttachmentView = fb.useDepth ? fb.getDepthTexture() : null;
        var renderSources = this.renderSources.get();
        GpuBuffer vertexBuffer = renderSources.getUpdatedVertexBuffer();
        RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSources.mandelbrotSetShapeIndexBuffer;
        GpuBuffer indexBuffer = shapeIndexBuffer.getIndexBuffer(6);
        VertexFormat.IndexType indexType = shapeIndexBuffer.getIndexType();
        var camPos = context.camera().getPosition();
        var modelViewMat = new Matrix4f(RenderSystem.getModelViewMatrix()).translate((float)-camPos.x, (float)-camPos.y, (float)-camPos.z);
        modelViewMat.swap(RenderSystem.getModelViewMatrix());
        try(RenderPass renderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(colorAttachmentView, OptionalInt.empty(), depthAttachmentView, OptionalDouble.empty())){
            renderPass.setPipeline(RenderSources.mandelbrotSetPipeline);
            renderPass.setUniform("setColor", new float[]{0, 0, 0, 1});
            renderPass.setUniform("outColor", new float[]{1, 1, 1, 1});
            renderPass.setUniform("maxDepth", maxDepth.getAsInt());
            renderPass.setVertexBuffer(0, vertexBuffer);
            renderPass.setIndexBuffer(indexBuffer, indexType);
            renderPass.drawIndexed(0, 6);
        }
        modelViewMat.swap(RenderSystem.getModelViewMatrix());
    }
}
