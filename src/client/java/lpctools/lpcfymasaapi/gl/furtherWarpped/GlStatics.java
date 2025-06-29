package lpctools.lpcfymasaapi.gl.furtherWarpped;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import lpctools.lpcfymasaapi.gl.Constants;
import lpctools.util.javaex.AutoCloseableNoExcept;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;

import static org.lwjgl.opengl.GL20.glBlendEquationSeparate;

public interface GlStatics {
    static AutoCloseableNoExcept bindFrameBuffer(Framebuffer framebuffer){
        int lastFbo = GL33.glGetInteger(GL33.GL_FRAMEBUFFER_BINDING);
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, framebuffer.fbo);
        return ()->GL33.glBindFramebuffer(GlConst.GL_FRAMEBUFFER, lastFbo);
    }
    static AutoCloseableNoExcept bindDefaultFrameBuffer(){
        return bindFrameBuffer(MinecraftClient.getInstance().getFramebuffer());
    }
    static AutoCloseableNoExcept bindFrameBufferOrDefault(@Nullable Framebuffer framebuffer){
        if(framebuffer == null) return bindDefaultFrameBuffer();
        else return bindFrameBuffer(framebuffer);
    }
    static void setBlend(Constants.BlendFactor srcRGB, Constants.BlendFactor dstRGB,
                         Constants.BlendFactor srcA, Constants.BlendFactor dstA,
                         Constants.BlendEquation eqRGB, Constants.BlendEquation eqA) {
        Constants.BlendFactor.blendFuncSeparate(srcRGB, dstRGB, srcA, dstA);
        glBlendEquationSeparate(eqRGB.value, eqA.value);
    }
}
