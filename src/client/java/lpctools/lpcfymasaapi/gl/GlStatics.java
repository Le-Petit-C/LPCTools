package lpctools.lpcfymasaapi.gl;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public interface GlStatics {
    static RenderPass bindFrameBuffer(Framebuffer framebuffer){
        return RenderSystem.getDevice().createCommandEncoder()
            .createRenderPass(() -> "LPCTools RenderPass",
                framebuffer.getColorAttachmentView(),
                OptionalInt.empty(),
                framebuffer.useDepthAttachment ? framebuffer.getDepthAttachmentView() : null,
                OptionalDouble.empty());
    }
    static RenderPass bindDefaultFrameBuffer(){
        return bindFrameBuffer(MinecraftClient.getInstance().getFramebuffer());
    }
    static RenderPass bindFrameBufferOrDefault(@Nullable Framebuffer framebuffer){
        if(framebuffer == null) return bindDefaultFrameBuffer();
        else return bindFrameBuffer(framebuffer);
    }
}
