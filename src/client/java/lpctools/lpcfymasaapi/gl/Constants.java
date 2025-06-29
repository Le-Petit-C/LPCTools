package lpctools.lpcfymasaapi.gl;

import static org.lwjgl.opengl.GL33.*;
import static lpctools.lpcfymasaapi.gl.furtherWarpped.RestorableOption.*;

@SuppressWarnings("unused")
public interface Constants {
    enum DrawMode{
        POINTS(GL_POINTS),
        LINES(GL_LINES),
        LINE_LOOP(GL_LINE_LOOP),
        LINE_STRIP(GL_LINE_STRIP),
        TRIANGLES(GL_TRIANGLES),
        TRIANGLE_STRIP(GL_TRIANGLE_STRIP),
        TRIANGLE_FAN(GL_TRIANGLE_FAN),
        QUADS(GL_QUADS),
        QUAD_STRIP(GL_QUAD_STRIP),
        POLYGON(GL_POLYGON);
        DrawMode(int value){this.value = value;}
        public void drawElements(int count, IndexType type){
            glDrawElements(value, count, type.value, 0);}
        public void drawArrays(int first, int count){
            glDrawArrays(value, first, count);}
        public void drawArraysInstanced(int first, int count, int primCount){
            glDrawArraysInstanced(value, first, count, primCount);}
        public void drawElementsInstanced(int count, IndexType type, int instanceCount){
            glDrawElementsInstanced(value, count, type.value, 0, instanceCount);}
        public final int value;
    }
    enum DataType{
        BYTE(GL_BYTE, 1),
        UNSIGNED_BYTE(GL_UNSIGNED_BYTE, 1),
        SHORT(GL_SHORT, 2),
        UNSIGNED_SHORT(GL_UNSIGNED_SHORT, 2),
        INT(GL_INT, 4),
        UNSIGNED_INT(GL_UNSIGNED_INT, 4),
        FLOAT(GL_FLOAT, 4),
        _2_BYTES(GL_2_BYTES, 2),
        _3_BYTES(GL_3_BYTES, 3),
        _4_BYTES(GL_4_BYTES, 4),
        DOUBLE(GL_DOUBLE, 8);
        DataType(int value, int size){this.value = value;this.size = size;}
        public final int value, size;
    }
    enum BufferMode{
        STREAM_DRAW(GL_STREAM_DRAW),
        STREAM_READ(GL_STREAM_READ),
        STREAM_COPY(GL_STREAM_COPY),
        STATIC_DRAW(GL_STATIC_DRAW),
        STATIC_READ(GL_STATIC_READ),
        STATIC_COPY(GL_STATIC_COPY),
        DYNAMIC_DRAW(GL_DYNAMIC_DRAW),
        DYNAMIC_READ(GL_DYNAMIC_READ),
        DYNAMIC_COPY(GL_DYNAMIC_COPY);
        BufferMode(int value){this.value = value;}
        public final int value;
    }
    enum EnableMask implements SimpleEnableOption {
        BLEND(GL_BLEND),
        CULL_FACE(GL_CULL_FACE),
        DEPTH_TEST(GL_DEPTH_TEST);
        EnableMask(int value){this.value = value;}
        public final int value;
        @Override public void enable(){glEnable(value);}
        @Override public void disable(){glDisable(value);}
        @Override public void enable(boolean b){if(b) enable();else disable();}
        @Override public boolean isEnabled(){return glIsEnabled(value);}
    }
    enum IndexType{
        BYTE(GL_UNSIGNED_BYTE),
        SHORT(GL_UNSIGNED_SHORT),
        INT(GL_UNSIGNED_INT);
        IndexType(int value){this.value = value;}
        public final int value;
    }
    enum BufferType{
        ARRAY_BUFFER(GL_ARRAY_BUFFER),
        ELEMENT_ARRAY_BUFFER(GL_ELEMENT_ARRAY_BUFFER),
        TEXTURE_BUFFER(GL_TEXTURE_BUFFER);
        BufferType(int value){this.value = value;}
        public final int value;
        public void bind(Buffer buffer){glBindBuffer(value, buffer.getGlBufferId());}
        public void unbind(){glBindBuffer(value, 0);}
    }
    enum BlendFactor{
        ZERO(GL_ZERO), ONE(GL_ONE),
        SRC_COLOR(GL_SRC_COLOR), DST_COLOR(GL_DST_COLOR),
        ONE_MINUS_SRC_COLOR(GL_ONE_MINUS_SRC_COLOR),
        ONE_MINUS_DST_COLOR(GL_ONE_MINUS_DST_COLOR),
        SRC_ALPHA(GL_SRC_ALPHA), DST_ALPHA(GL_DST_ALPHA),
        ONE_MINUS_SRC_ALPHA(GL_ONE_MINUS_SRC_ALPHA),
        ONE_MINUS_DST_ALPHA(GL_ONE_MINUS_DST_ALPHA),
        CONSTANT_COLOR(GL_CONSTANT_COLOR),
        ONE_MINUS_CONSTANT_COLOR(GL_ONE_MINUS_CONSTANT_COLOR),
        CONSTANT_ALPHA(GL_CONSTANT_ALPHA),
        ONE_MINUS_CONSTANT_ALPHA(GL_ONE_MINUS_CONSTANT_ALPHA);
        public final int value;
        BlendFactor(int value) { this.value = value; }
        public static void blendFuncSeparate(BlendFactor srcRGB, BlendFactor dstRGB, BlendFactor srcA, BlendFactor dstA){
            glBlendFuncSeparate(srcRGB.value, dstRGB.value, srcA.value, dstA.value);
        }
    }
    enum BlendEquation {
        ADD(GL_FUNC_ADD),
        SUBTRACT(GL_FUNC_SUBTRACT),
        REVERSE_SUBTRACT(GL_FUNC_REVERSE_SUBTRACT);
        public final int value;
        BlendEquation(int value) { this.value = value; }
        public static void blendEquationSeparate(BlendEquation eqRGB, BlendEquation eqA){
            glBlendEquationSeparate(eqRGB.value, eqA.value);
        }
    }
}
