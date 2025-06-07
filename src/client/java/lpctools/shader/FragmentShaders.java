package lpctools.shader;

import lpctools.lpcfymasaapi.gl.Shader;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

public class FragmentShaders {
    public static void init(){}
    public static final Shader vertex_color = newLPCFrag("vertex_color.glsl");
    public static final Shader static_color = newLPCFrag("static_color.glsl");
    public static Shader newLPCFrag(String file){
        return new Shader(Identifier.of("lpctools","shaders/fragments/" + file), GL30.GL_FRAGMENT_SHADER);
    }
}
