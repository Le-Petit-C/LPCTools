package lpctools.lpcfymasaapi.gl.furtherWarpped;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.memAddress;

public interface RestorableOptions {
    RestorableOption.EnableOption DEPTH_WRITE = new RestorableOption.SimpleEnableOption() {
        @Override public void enable(boolean b) {glDepthMask(b);}
        @Override public boolean isEnabled() {return glGetBoolean(GL_DEPTH_WRITEMASK);}
    };
    RestorableOption.EnableOption COLOR_WRITE = new RestorableOption.EnableOption() {
        @Override public void enable(boolean b) {glColorMask(b, b, b, b);}
        @Override public void push(@NotNull IntArrayList list) {
            try(MemoryStack stack = stackGet()){
                stack.push();
                ByteBuffer params = stack.calloc(4);
                nglGetBooleanv(GL_COLOR_WRITEMASK, memAddress(params));
                list.add(params.getInt(0));
            }
        }
        @Override public void pop(@NotNull IntArrayList list) {
            int value = list.removeLast();
            glColorMask((value & 0x000000ff) != 0, (value & 0x0000ff00) != 0, (value & 0x00ff0000) != 0, (value & 0xff000000) != 0);
        }
    };
    RestorableOption BLEND_STATE = new RestorableOption() {
        @Override public void push(@NotNull IntArrayList list) {
            list.add(glGetInteger(GL_BLEND_SRC_RGB));
            list.add(glGetInteger(GL_BLEND_DST_RGB));
            list.add(glGetInteger(GL_BLEND_SRC_ALPHA));
            list.add(glGetInteger(GL_BLEND_DST_ALPHA));
            list.add(glGetInteger(GL_BLEND_EQUATION_RGB));
            list.add(glGetInteger(GL_BLEND_EQUATION_ALPHA));
        }
        @Override public void pop(@NotNull IntArrayList list) {
            int eqA   = list.removeLast();
            int eqRGB = list.removeLast();
            int dstA  = list.removeLast();
            int srcA  = list.removeLast();
            int dstRGB= list.removeLast();
            int srcRGB= list.removeLast();
            glBlendFuncSeparate(srcRGB, dstRGB, srcA, dstA);
            glBlendEquationSeparate(eqRGB, eqA);
        }
    };
}
