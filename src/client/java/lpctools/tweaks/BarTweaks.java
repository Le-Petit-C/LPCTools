package lpctools.tweaks;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class BarTweaks {
    public static final ThirdListConfig barTweaks = new ThirdListConfig(TweakConfigs.tweaks, "barTweaks", null) {
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
            super.getButtonOptions(res);
            if(isExpanded()) return;
            res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, expBarDisplaysLocatorPoints));
            res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, jumpBarDisplaysLocatorPoints));
            res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, locatorBarUsesExpBackground));
            res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, creativeShowsExperienceBar));
            res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, creativeShowsStatusBar));
        }
    };
    static {listStack.push(barTweaks);}
    public static final BooleanConfig expBarDisplaysLocatorPoints = addBooleanConfig("expBarDisplaysLocatorPoints", false);
    public static final BooleanConfig jumpBarDisplaysLocatorPoints = addBooleanConfig("jumpBarDisplaysLocatorPoints", false);
    public static final BooleanConfig locatorBarUsesExpBackground = addBooleanConfig("locatorBarUsesExpBackground", false);
    public static final BooleanConfig creativeShowsExperienceBar = addBooleanConfig("creativeShowsExperienceBar", false);
    public static final BooleanConfig creativeShowsStatusBar = addBooleanConfig("creativeShowsStatusBar", false);
    static {listStack.pop();}
}
