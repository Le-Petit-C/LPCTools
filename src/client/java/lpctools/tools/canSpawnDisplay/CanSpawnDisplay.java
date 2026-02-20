package lpctools.tools.canSpawnDisplay;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.tools.ToolConfigs;
import net.minecraft.client.MinecraftClient;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;
import static lpctools.tools.canSpawnDisplay.CanSpawnDisplayData.*;

public class CanSpawnDisplay{
    public static final BooleanHotkeyThirdListConfig CSConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "CS", CanSpawnDisplay::switchCallback);
    static {setLPCToolsToggleText(CSConfig);}
    static {listStack.push(CSConfig);}
    public static final ColorConfig displayColor = addColorConfig("displayColor", Color4f.fromColor(0x7fffffff), CanSpawnDisplay::onColorChanged);
    public static final RangeLimitConfig rangeLimit = addRangeLimitConfig();
    static {rangeLimit.setValueChangeCallback(CanSpawnDisplay::onRenderRangeChanged);}
    // public static final DoubleConfig renderDistance = addDoubleConfig("renderDistance", 32, 16, 512);
    public static final RenderMethodConfig renderMethod = addConfig(new RenderMethodConfig());
    public static final BooleanConfig renderXRays = addBooleanConfig("renderXRays", true, CanSpawnDisplay::onRenderXRaysChanged);
    static {listStack.pop();}
    
    private static void onColorChanged() { if(dataInstance != null) dataInstance.updateRenderColor(); }
    private static void onRenderRangeChanged(){ if(dataInstance != null) dataInstance.updateRenderRange(); }
    private static void onRenderXRaysChanged(){ if(dataInstance != null) dataInstance.updateRenderXRays(); }
    
    public static class RenderMethodConfig extends ArrayOptionListConfig<IRenderMethod>{
        public RenderMethodConfig() {
            super(CSConfig, "renderMethod");
            for(IRenderMethod method : renderMethods)
                addOption(getFullTranslationKey() + '.' + method.getNameKey(), method);
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            if(dataInstance != null) dataInstance.updateRenderMethod();
        }
    }
    public static void switchCallback() {
        boolean currentValue = CSConfig.getBooleanValue();
        if(currentValue) dataInstance = new DataInstance(MinecraftClient.getInstance());
        else if(dataInstance != null){
            dataInstance.close();
            dataInstance = null;
        }
    }
}
