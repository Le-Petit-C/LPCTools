package lpctools.debugs;

import com.mojang.blaze3d.platform.GlConst;
import lpctools.lpcfymasaapi.gl.Shader;
import lpctools.util.DataUtils;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

public class RenderTest implements SimpleSynchronousResourceReloadListener {
    public static final RenderTest instance = new RenderTest();
    public static void init(){instance._init();}
    public void _init(){
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(this);
    }
    @Override public Identifier getFabricId() {
        return Identifier.of("lpctools", "shader_loader");
    }
    public static final Identifier simple_translation_vertexId = LPCVertexShaderId("simple_translation.glsl");
    public static final Shader simple_translation_vertex_shader = new Shader(simple_translation_vertexId, GL30.GL_VERTEX_SHADER);
    public static int simple_translation_vertex = 0;
    public static final Identifier no_change_fragId = LPCFragmentShaderId("no_change.glsl");
    public static final Shader no_change_frag_shader = new Shader(no_change_fragId, GL30.GL_FRAGMENT_SHADER);
    public static int no_change_frag = 0;
    @Override public void reload(ResourceManager manager) {
        no_change_frag = compileShader(no_change_frag, GlConst.GL_FRAGMENT_SHADER, manager, no_change_fragId);
        simple_translation_vertex = compileShader(simple_translation_vertex, GlConst.GL_VERTEX_SHADER, manager, simple_translation_vertexId);
    }
    public static int compileShader(int shader, int shaderType, ResourceManager manager, Identifier shaderId){
        if(shader <= 0) shader = GL30.glCreateShader(shaderType);
        String code = DataUtils.getTextFileResource(manager, shaderId);
        if(code != null){
            GL30.glShaderSource(shader, code);
            GL30.glCompileShader(shader);
            return shader;
        }
        GL30.glDeleteShader(shader);
        return 0;
    }
    public static Identifier LPCVertexShaderId(String shaderFileName){
        return Identifier.of("lpctools","shaders/vertexes/" + shaderFileName);
    }
    public static Identifier LPCFragmentShaderId(String shaderFileName){
        return Identifier.of("lpctools","shaders/fragments/" + shaderFileName);
    }
}
