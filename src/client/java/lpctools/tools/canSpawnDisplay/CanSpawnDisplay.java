package lpctools.tools.canSpawnDisplay;

import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.RangeLimitConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.ColorConfig;
import lpctools.tools.ToolUtils;
import lpctools.tools.ToolWithRunnerConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;
import static lpctools.tools.canSpawnDisplay.CanSpawnDisplayData.*;

public class CanSpawnDisplay {
    public static final ToolWithRunnerConfig<CanSpawnDisplayRunner> CSConfig = ToolUtils.configBuilder("CS").withToolRunner(CanSpawnDisplayRunner::new).build();
    static {listStack.push(CSConfig);}
    public static final ColorConfig displayColor = addColorConfig("displayColor", Color4f.fromColor(0x7fffffff), CSConfig.runnerApplyCallback(CanSpawnDisplayRunner::updateRenderColor));
    public static final RangeLimitConfig rangeLimit = addRangeLimitConfig();
    static {rangeLimit.setValueChangeCallback(CSConfig.runnerApplyCallback(CanSpawnDisplayRunner::updateRenderRange));}
    // public static final DoubleConfig renderDistance = addDoubleConfig("renderDistance", 32, 16, 512);
    public static final RenderMethodConfig renderMethod = addConfig(new RenderMethodConfig());
    public static final BooleanConfig renderXRays = addBooleanConfig("renderXRays", true, CSConfig.runnerApplyCallback(CanSpawnDisplayRunner::updateRenderXRays));
    static {listStack.pop();}
    
    public static class RenderMethodConfig extends ArrayOptionListConfig<IRenderMethod>{
        public RenderMethodConfig() {
            super(CSConfig, "renderMethod");
            for(IRenderMethod method : renderMethods)
                addOption(getFullTranslationKey() + '.' + method.getNameKey(), method);
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            CSConfig.applyToRunnerIfPresent(CanSpawnDisplayRunner::updateRenderMethod);
        }
    }
}
