package lpctools.lpcfymasaapi.gl;

import net.minecraft.resource.ResourceManager;
import org.lwjgl.opengl.GL30;

import java.util.function.Consumer;

public class Program implements AutoCloseable, Consumer<ResourceManager> {
    public final Shader vertexShader;
    public final Shader fragmentShader;
    private int glProgramId;
    public int getGlProgramId(){return glProgramId;}
    Program(Shader vertexShader, Shader fragmentShader){
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
        Initializer.register(this);
    }
    @Override public void close() {
        Initializer.unregister(this);
        GL30.glDeleteProgram(glProgramId);
    }
    @Override public void accept(ResourceManager manager) {
        if(glProgramId == 0) glProgramId = GL30.glCreateProgram();
        GL30.glAttachShader(glProgramId, vertexShader.getGlShaderId());
        GL30.glAttachShader(glProgramId, fragmentShader.getGlShaderId());
        GL30.glLinkProgram(glProgramId);
    }
}
