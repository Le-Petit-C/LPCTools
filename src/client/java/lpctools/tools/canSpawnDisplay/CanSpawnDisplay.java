package lpctools.tools.canSpawnDisplay;

import fi.dy.masa.malilib.util.Color4f;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanHotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.ColorConfig;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import net.minecraft.client.MinecraftClient;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.ToolUtils.*;

public class CanSpawnDisplay extends ThirdListConfig implements AutoCloseable{
    public final CanSpawnDisplaySwitch canSpawnDisplay;
    public final ColorConfig displayColor;
    public final RangeLimitConfig rangeLimit;
    public final DoubleConfig renderDistance;
    public final RenderMethodConfig renderMethod;
    public final BooleanConfig renderXRays;
    public final MinecraftClient client;
    @Override public void close() throws Exception {rangeLimit.close();}
    public class RenderMethodConfig extends ArrayOptionListConfig<IRenderMethod>{
        public RenderMethodConfig() {
            super(CanSpawnDisplay.this, "renderMethod");
            for(IRenderMethod method : renderMethods)
                addOption(getFullTranslationKey() + '.' + method.getNameKey(), method);
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            if(renderInstance != null) renderInstance.setRenderMethod(get());
        }
    }
    public class CanSpawnDisplaySwitch extends BooleanHotkeyConfig{
        public CanSpawnDisplaySwitch() {
            super(CanSpawnDisplay.this, "canSpawnDisplay", false, null);
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            boolean currentValue = getBooleanValue();
            if(currentValue) renderInstance = new RenderInstance(client, CanSpawnDisplay.this);
            else{
                if(renderInstance != null){
                    renderInstance.close();
                    renderInstance = null;
                }
            }
        }
    }
    public CanSpawnDisplay(ILPCConfigList parent, MinecraftClient client){
        super(parent, "CS", false);
        this.client = client;
        try(ConfigListLayer ignored = new ConfigListLayer(this)){
            canSpawnDisplay = addConfig(new CanSpawnDisplaySwitch());
            setLPCToolsToggleText(canSpawnDisplay);
            //noinspection deprecation
            displayColor = addColorConfig("displayColor", Color4f.fromColor(0x7fffffff));
            rangeLimit = addRangeLimitConfig(false);
            rangeLimit.setValueChangeCallback(()->{if(renderInstance != null) renderInstance.onRenderRangeChanged(rangeLimit);});
            renderDistance = addDoubleConfig("renderDistance", 32, 16, 512);
            renderMethod = addConfig(new RenderMethodConfig());
            renderXRays = addBooleanConfig("renderXRays", true);
        }
    }
    private static final IRenderMethod[] renderMethods = {
        new MinihudStyleRenderMethod(),
        new FullSurfaceRenderMethod(),
        new LineCubeRenderMethod()
    };
    private RenderInstance renderInstance;
}
