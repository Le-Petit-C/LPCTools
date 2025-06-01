package lpctools.lpcfymasaapi.gl;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

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
}
