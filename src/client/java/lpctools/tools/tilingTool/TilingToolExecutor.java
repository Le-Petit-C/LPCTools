package lpctools.tools.tilingTool;

import lpctools.lpcfymasaapi.Registries;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

//TODO

public class TilingToolExecutor implements AutoCloseable, ClientTickEvents.EndTick{
    TilingToolExecutor(){registerAll(true);}
    @Override public void close() {registerAll(false);}
    private void registerAll(boolean b){
        Registries.END_CLIENT_TICK.register(this, b);
    }
    @Override public void onEndTick(MinecraftClient minecraftClient) {
    
    }
}
