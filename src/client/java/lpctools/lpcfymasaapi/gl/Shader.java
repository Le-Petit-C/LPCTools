package lpctools.lpcfymasaapi.gl;

import lpctools.util.DataUtils;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

public class Shader implements AutoCloseable, Consumer<ResourceManager> {
    private int glShaderId = 0;
    public final int shaderType;
    public final Identifier resourceId;
    public int getGlShaderId(){return glShaderId;}
    //GL_FRAGMENT_SHADER or GL_VERTEX_SHADER for shaderType
    public Shader(Identifier resourceId, int shaderType){
        this.resourceId = resourceId;
        this.shaderType = shaderType;
        LPCGLInitializer.register(this);
    }
    @Override public void close() {
        LPCGLInitializer.unregister(this);
        GL30.glDeleteShader(glShaderId);
        glShaderId = 0;
    }
    @Override public void accept(ResourceManager manager) {
        String code = DataUtils.getTextFileResource(manager, resourceId);
        if(code == null) return;
        if(glShaderId == 0) glShaderId = GL30.glCreateShader(shaderType);
        GL30.glShaderSource(glShaderId, code);
        GL30.glCompileShader(glShaderId);
    }
}
