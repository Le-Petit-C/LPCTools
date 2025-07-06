package lpctools.tools.canSpawnDisplay;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.tools.ToolConfigs;
import net.minecraft.client.MinecraftClient;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.canSpawnDisplay.CanSpawnDisplayData.*;

public class CanSpawnDisplay{
    public static final ThirdListConfig CSConfig = new ThirdListConfig(ToolConfigs.toolConfigs, "CS", false);
    static {listStack.push(CSConfig);}
    public static final CanSpawnDisplaySwitch canSpawnDisplay = addConfig(new CanSpawnDisplaySwitch());
    static {setLPCToolsToggleText(canSpawnDisplay);}
    public static final ColorConfig displayColor = addColorConfig("displayColor", Color4f.fromColor(0x7fffffff));
    public static final RangeLimitConfig rangeLimit = addRangeLimitConfig(false);
    static {rangeLimit.setValueChangeCallback(CanSpawnDisplay::onRenderRangeChanged);}
    public static final DoubleConfig renderDistance = addDoubleConfig("renderDistance", 32, 16, 512);
    public static final RenderMethodConfig renderMethod = addConfig(new RenderMethodConfig());
    public static final BooleanConfig renderXRays = addBooleanConfig("renderXRays", true);
    static {listStack.pop();}
    private static void onRenderRangeChanged(){if(renderInstance != null) renderInstance.onRenderRangeChanged(rangeLimit);}
    
    public static class RenderMethodConfig extends ArrayOptionListConfig<IRenderMethod>{
        public RenderMethodConfig() {
            super(CSConfig, "renderMethod");
            for(IRenderMethod method : renderMethods)
                addOption(getFullTranslationKey() + '.' + method.getNameKey(), method);
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            if(renderInstance != null) renderInstance.setRenderMethod(get());
            if(renderInstance != null) renderInstance.resetRender();
        }
    }
    public static class CanSpawnDisplaySwitch extends BooleanHotkeyConfig{
        public CanSpawnDisplaySwitch() {
            super(CSConfig, "canSpawnDisplay", false, null);
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            boolean currentValue = getBooleanValue();
            if(currentValue) renderInstance = new RenderInstance(MinecraftClient.getInstance());
            else if(renderInstance != null){
                renderInstance.close();
                renderInstance = null;
            }
        }
    }
}
