package lpctools.debugs;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import static lpctools.lpcfymasaapi.Registry.*;

public class MandelbrotSetRender extends ThirdListConfig implements WorldRenderEvents.DebugRender {
    public MandelbrotSetRender(ILPCConfigList parent) {
        super(parent, "mandelbrotSet", false);
        ILPCValueChangeCallback onValueChanged = () -> {
            if (getAsBoolean()) registerWorldRenderBeforeDebugRenderCallback(this);
            else unregisterWorldRenderBeforeDebugRenderCallback(this);
        };
        setValueChangeCallback(onValueChanged);
    }
    
    @Override public void beforeDebugRender(WorldRenderContext worldRenderContext) {
    
    }
}
