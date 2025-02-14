package lpctools.lpcfymasaapi;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;

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
    public static void runEndClientTickCallbacks(MinecraftClient client){
        for(ClientTickEvents.EndTick callback : endClientTickCallbacks)
            callback.onEndTick(client);
    }
    public static void runInGameEndMouseCallbacks(int button, int action, int mods){
        for(InGameEndMouse callback : inGameEndMouseCallbacks)
            callback.onInGameEndMouse(button, action, mods);
    }
    static void init(){
        ClientTickEvents.END_CLIENT_TICK.register(runner);
    }
    @NotNull private static final LinkedHashSet<Registry.InGameEndMouse> inGameEndMouseCallbacks = new LinkedHashSet<>();
    @NotNull private static final LinkedHashSet<ClientTickEvents.EndTick> endClientTickCallbacks = new LinkedHashSet<>();
    @NotNull private static final CallbackRunner runner = new CallbackRunner();
    private static class CallbackRunner implements ClientTickEvents.EndTick {
        @Override
        public void onEndTick(MinecraftClient client) {
            runEndClientTickCallbacks(client);
        }
    }
    public interface InGameEndMouse {
        void onInGameEndMouse(int button, int action, int mods);
    }
}
