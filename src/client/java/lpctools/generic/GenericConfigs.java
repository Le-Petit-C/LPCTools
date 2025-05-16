package lpctools.generic;

import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ConfigOpenGuiConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class GenericConfigs {
    public static void init(){
        configOpenGuiConfig = addConfigOpenGuiConfig("Z,C");
    }

    static ConfigOpenGuiConfig configOpenGuiConfig;
}
