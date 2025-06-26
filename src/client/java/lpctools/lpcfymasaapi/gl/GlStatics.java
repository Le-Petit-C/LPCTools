package lpctools.lpcfymasaapi.gl;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public interface GlStatics {
    static RenderPass bindFrameBuffer(Framebuffer framebuffer){
        GpuTexture colorTexture = framebuffer.getColorAttachment();
        GpuTexture depthTexture = framebuffer.getDepthAttachment();
        return RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty());
    }
    static RenderPass bindDefaultFrameBuffer(){
        return bindFrameBuffer(MinecraftClient.getInstance().getFramebuffer());
    }
    static RenderPass bindFrameBufferOrDefault(@Nullable Framebuffer framebuffer){
        if(framebuffer == null) return bindDefaultFrameBuffer();
        else return bindFrameBuffer(framebuffer);
    }
}
