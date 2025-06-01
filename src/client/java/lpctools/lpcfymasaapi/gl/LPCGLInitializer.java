package lpctools.lpcfymasaapi.gl;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.HashSet;

public class LPCGLInitializer {
    static ResourceManager manager;
    public static void init(){}
    static final HashSet<Shader> shaders = new HashSet<>();
    static final HashSet<Program<?>> programs = new HashSet<>();
    static final HashSet<UniformData> uniforms = new HashSet<>();
    static final HashSet<Buffer> buffers = new HashSet<>();
    static final HashSet<VertexArray> vertexArrays = new HashSet<>();
    static {
        Identifier lpcShaderResourceReloadCallbackId
            = Identifier.of("lpctools", "shader_reload");
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener(){
                @Override public Identifier getFabricId() {
                    return lpcShaderResourceReloadCallbackId;
                }
                @Override public void reload(ResourceManager manager) {
                    LPCGLInitializer.manager = manager;
                    for(Shader shader : shaders) shader.reloadAndCompile();
                    for(Program<?> program : programs) program.attachAndLink();
                    for(UniformData uniformData : uniforms) uniformData.updateLocation();
                    for(Buffer buffer : buffers) buffer.gen();
                    for(VertexArray vertexArray : vertexArrays) vertexArray.gen();
                }
            });
    }
    static boolean initialized(){return manager != null;}
}
