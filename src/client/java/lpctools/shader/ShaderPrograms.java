package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Program;
import lpctools.lpcfymasaapi.gl.furtherWarpped.VertexTypes;
import lpctools.util.DataUtils;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static lpctools.lpcfymasaapi.gl.Uniform.*;

@SuppressWarnings("unused")
public class ShaderPrograms {
    public static final PositionColorProgram POSITION_COLOR_PROGRAM = new PositionColorProgram();
    public static final PositionStaticColorProgram POSITION_STATIC_COLOR_PROGRAM = new PositionStaticColorProgram();
    
    public interface WithProjectionMatrix{ void setProjectionMatrix(Matrix4f matrix);}
    public interface WithModelViewMatrix{ void setModelMatrix(Matrix4f matrix);}
    public interface WithFinalMatrix{ void setFinalMatrix(Matrix4f matrix);}
    public interface WithStaticColor {
        void setColor4f(Vector4f color);
        void setColor32(int color);
        @Contract(pure = true)
        static int color4f2color32(Vector4f color){
            return normalizeToByte(color.x)
                | (normalizeToByte(color.y) << 8)
                | (normalizeToByte(color.z) << 16)
                | (normalizeToByte(color.w) << 24);
        }
        @Contract(pure = true)
        static Vector4f color322color4f(int color){
            return new Vector4f(
                normalizeFromByte((byte) (color & 0xff)),
                normalizeFromByte((byte) ((color >>> 8) & 0xff)),
                normalizeFromByte((byte) ((color >>> 16) & 0xff)),
                normalizeFromByte((byte) ((color >>> 24) & 0xff))
            );
        }
        @Contract(pure = true)
        static byte normalizeToByte(float value){
            return (byte)Math.clamp(value * 255, 0, 255);
        }
        @Contract(pure = true)
        static float normalizeFromByte(byte value){
            return Byte.toUnsignedInt(value) / 255.0f;
        }
    }
    
    public static class PositionColorProgram extends Program implements WithFinalMatrix{
        public final UniformMatrix4f matrixUniform = addUniform(new UniformMatrix4f(this, "matrix"));
        public PositionColorProgram() {super(VertexShaders.position_translation_color_pass_through, FragmentShaders.vertex_color, VertexTypes.POSITION_COLOR);}
        @Override public void setFinalMatrix(Matrix4f matrix) {matrixUniform.set(matrix);}
    }
    public static class PositionStaticColorProgram extends Program implements WithFinalMatrix, WithStaticColor {
        public final UniformMatrix4f matrixUniform = addUniform(new UniformMatrix4f(this, "matrix"));
        public final Uniform4f colorUniform = addUniform(new Uniform4f(this, "color"));
        public PositionStaticColorProgram() {super(VertexShaders.position_translation, FragmentShaders.static_color, VertexTypes.POSITION);}
        @Override public void setFinalMatrix(Matrix4f matrix) {matrixUniform.set(matrix);}
        @Override public void setColor4f(Vector4f color) {colorUniform.set(color);}
        @Override public void setColor32(int color) {setColor4f(DataUtils.argb2VectorABGRf(color));}
    }
    
    public static void init(){}
}
