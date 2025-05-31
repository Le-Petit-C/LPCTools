package lpctools.lpcfymasaapi.gl;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

public class Initializer {
    static ResourceManager manager;
    private static final LinkedHashSet<Consumer<ResourceManager>>
        resourceReloadListeners = new LinkedHashSet<>();
    public static void register(Consumer<ResourceManager> callback){
        resourceReloadListeners.add(callback);
        if(manager != null) callback.accept(manager);
    }
    public static void unregister(Consumer<ResourceManager> callback){
        resourceReloadListeners.remove(callback);
    }
    public static void init(){}
    static {
        Identifier lpcShaderResourceReloadCallbackId
            = Identifier.of("lpctools", "shader_reload");
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener(){
                @Override public Identifier getFabricId() {
                    return lpcShaderResourceReloadCallbackId;
                }
                @Override public void reload(ResourceManager manager) {
                    Initializer.manager = manager;
                    for(Consumer<ResourceManager> shader : resourceReloadListeners)
                        shader.accept(manager);
                }
            });
    }
}
