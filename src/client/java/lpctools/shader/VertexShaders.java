package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Shader;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

public class VertexShaders {
    public static void init(){}
    public static final Shader position_translation_color_pass_through = newLPCVert("position_translation_color_pass_through.glsl");
    public static final Shader position_translation = newLPCVert("position_translation.glsl");
    public static Shader newLPCVert(String file){
        return new Shader(Identifier.of("lpctools","shaders/vertexes/" + file), GL30.GL_VERTEX_SHADER);
    }
}
