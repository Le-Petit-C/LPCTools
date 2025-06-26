package lpctools.lpcfymasaapi.gl;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.memAddress;

@SuppressWarnings("unused")
public class Constants {
    public enum DrawMode{
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
    public enum DataType{
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
    public enum BufferMode{
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
    public enum EnableMask implements SimpleEnableOption {
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
    public enum IndexType{
        BYTE(GL_UNSIGNED_BYTE),
        SHORT(GL_UNSIGNED_SHORT),
        INT(GL_UNSIGNED_INT);
        IndexType(int value){this.value = value;}
        public final int value;
    }
    public enum BufferType{
        ARRAY_BUFFER(GL_ARRAY_BUFFER),
        ELEMENT_ARRAY_BUFFER(GL_ELEMENT_ARRAY_BUFFER),
        TEXTURE_BUFFER(GL_TEXTURE_BUFFER),
        SHADER_STORAGE_BUFFER(GL_SHADER_STORAGE_BUFFER);
        BufferType(int value){this.value = value;}
        public final int value;
        public void bind(Buffer buffer){glBindBuffer(value, buffer.getGlBufferId());}
        public void unbind(){glBindBuffer(value, 0);}
    }
    public interface SimpleEnableOption extends EnableOption{
        boolean isEnabled();
        @Override default void push(@NotNull ByteArrayList list){list.add((byte)(isEnabled() ? 1 : 0));}
        @Override default void pop(@NotNull ByteArrayList list){enable(list.removeLast() != 0);}
    }
    public interface EnableOption extends RestorableOption {
        void enable(boolean b);
        default void enable(){enable(true);}
        default void disable(){enable(false);}
    }
    public interface RestorableOption {
        void push(@NotNull ByteArrayList list);
        void pop(@NotNull ByteArrayList list);
    }
    public interface EnableOptions{
        EnableOption DEPTH_WRITE = new SimpleEnableOption() {
            @Override public void enable(boolean b) {glDepthMask(b);}
            @Override public boolean isEnabled() {return glGetBoolean(GL_DEPTH_WRITEMASK);}
        };
        EnableOption COLOR_WRITE = new EnableOption() {
            @Override public void enable(boolean b) {glColorMask(b, b, b, b);}
            @Override public void push(@NotNull ByteArrayList list) {
                try(MemoryStack stack = stackGet()){
                    stack.push();
                    int stackPointer = stack.getPointer();
                    ByteBuffer params = stack.calloc(4);
                    nglGetBooleanv(GL_COLOR_WRITEMASK, memAddress(params));
                    list.add(params.get(3));
                    list.add(params.get(2));
                    list.add(params.get(1));
                    list.add(params.get(0));
                }
            }
            @Override public void pop(@NotNull ByteArrayList list) {
                glColorMask(list.removeLast() != 0, list.removeLast() != 0, list.removeLast() != 0, list.removeLast() != 0);
            }
        };
    }
}
