package lpctools.lpcfymasaapi;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

@SuppressWarnings("UnusedReturnValue")
public class Registry {
    public static boolean registerEndClientTickCallback(ClientTickEvents.EndTick callback){
        return endClientTickCallbacks.add(callback);
    }
    public static boolean unregisterEndClientTickCallback(ClientTickEvents.EndTick callback){
        return endClientTickCallbacks.remove(callback);
    }
    public static boolean registerInGameEndMouseCallback(Registry.InGameEndMouse callback){
        return inGameEndMouseCallbacks.add(callback);
    }
    public static boolean unregisterInGameEndMouseCallback(Registry.InGameEndMouse callback){
        return inGameEndMouseCallbacks.remove(callback);
    }
    public static boolean registerWorldRenderLastCallback(WorldRenderEvents.Last callback){
        return worldRenderLastCallbacks.add(callback);
    }
    public static boolean unregisterWorldRenderLastCallback(WorldRenderEvents.Last callback){
        return worldRenderLastCallbacks.remove(callback);
    }
    public static void runEndClientTickCallbacks(MinecraftClient client){
        for(ClientTickEvents.EndTick callback : endClientTickCallbacks)
            callback.onEndTick(client);
    }
    public static void runInGameEndMouseCallbacks(int button, int action, int mods){
        for(InGameEndMouse callback : inGameEndMouseCallbacks)
            callback.onInGameEndMouse(button, action, mods);
    }
    public static void runWorldRenderLastCallbacks(WorldRenderContext context){
        for(WorldRenderEvents.Last callback : worldRenderLastCallbacks)
            callback.onLast(context);
    }
    static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(Registry::runEndClientTickCallbacks);
        WorldRenderEvents.LAST.register(Registry::runWorldRenderLastCallbacks);
    }
    @NotNull private static final LinkedHashSet<Registry.InGameEndMouse> inGameEndMouseCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientTickEvents.EndTick> endClientTickCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<WorldRenderEvents.Last> worldRenderLastCallbacks = new LinkedHashSet<>();

    public interface InGameEndMouse {
        void onInGameEndMouse(int button, int action, int mods);
    }
}
