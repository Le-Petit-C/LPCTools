package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.implementations.ILPCConfigBase;

public class DerivedConfigUtils {
    public static String fullKeyByParent(ILPCConfigBase configBase){
        return configBase.getParent().getFullTranslationKey() + '.' + configBase.getNameKey();
    }
}
