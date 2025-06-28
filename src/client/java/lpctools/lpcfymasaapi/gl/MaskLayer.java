package lpctools.lpcfymasaapi.gl;

import com.mojang.blaze3d.systems.RenderPass;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import lpctools.lpcfymasaapi.gl.furtherWarpped.BlendPresets;
import lpctools.lpcfymasaapi.gl.furtherWarpped.GlStatics;
import lpctools.lpcfymasaapi.gl.furtherWarpped.RestorableOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.gl.Constants.*;
import static lpctools.lpcfymasaapi.gl.furtherWarpped.RestorableOption.*;
import static lpctools.lpcfymasaapi.gl.furtherWarpped.RestorableOptions.*;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class MaskLayer implements AutoCloseable{
    private final IntArrayList intArrayList = new IntArrayList();
    private final ArrayList<RestorableOption> masks;
    private final int lastProgram;
    private final int lastVertexArray;
    private final int lastArrayBuffer;
    private final RenderPass renderPass;
    private boolean restoredBlendState = false;
    public MaskLayer(){
        this(MinecraftClient.getInstance().getFramebuffer());
    }
    public MaskLayer(@Nullable Framebuffer framebuffer){
        masks = new ArrayList<>();
        lastProgram = GL33.glGetInteger(GL33.GL_CURRENT_PROGRAM);
        lastArrayBuffer = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING);
        lastVertexArray = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING);
        renderPass = GlStatics.bindFrameBufferOrDefault(framebuffer);
    }
    public MaskLayer restore(RestorableOption option){
        masks.add(option);
        option.push(intArrayList);
        return this;
    }
    @SuppressWarnings("resource")
    public MaskLayer enable(EnableOption option, boolean value){
        restore(option);
        option.enable(value);
        return this;
    }
    public void pop(){masks.removeLast().pop(intArrayList);}
    public void pop(int n){while(n-- > 0) pop();}
    public MaskLayer enable(EnableOption mask){return enable(mask, true);}
    public MaskLayer disable(EnableOption mask){return enable(mask, false);}
    public MaskLayer enableBlend(boolean value){return enable(EnableMask.BLEND, value);}
    public MaskLayer enableBlend(){return enableBlend(true);}
    public MaskLayer disableBlend(){return enableBlend(false);}
    public MaskLayer enableCullFace(boolean value){return enable(EnableMask.CULL_FACE, value);}
    public MaskLayer enableCullFace(){return enableCullFace(true);}
    public MaskLayer disableCullFace(){return enableCullFace(false);}
    public MaskLayer enableDepthTest(boolean value){return enable(EnableMask.DEPTH_TEST, value);}
    public MaskLayer enableDepthTest(){return enableDepthTest(true);}
    public MaskLayer disableDepthTest(){return enableDepthTest(false);}
    public MaskLayer enableDepthWrite(boolean value){return enable(DEPTH_WRITE, value);}
    public MaskLayer enableDepthWrite(){return enableDepthWrite(true);}
    public MaskLayer disableDepthWrite(){return enableDepthWrite(false);}
    public MaskLayer enableColorWrite(boolean value){return enable(COLOR_WRITE, value);}
    public MaskLayer enableColorWrite(){return enableColorWrite(true);}
    public MaskLayer disableColorWrite(){return enableColorWrite(false);}
    //为防止layer外的array binding导致“默认array被更改”，将VertexArray::bind()设为package-private并要求只能在此调用
    public void bindArray(VertexArray array){array.bind();}
    public MaskLayer blendState(BlendPresets.BlendPreset preset) {
        MaskLayer ret = restoredBlendState ? this : restore(BLEND_STATE);
        restoredBlendState = true;
        preset.apply();
        return ret;
    }
    @Override public void close() {
        while (!masks.isEmpty()) pop();
        renderPass.close();
        GL33.glBindVertexArray(lastVertexArray);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, lastArrayBuffer);
        GL33.glUseProgram(lastProgram);
    }
}
