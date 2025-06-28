package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;

public interface DerivedConfigUtils {
    static String fullKeyByParent(ILPCConfigBase configBase){
        return configBase.getParent().getFullTranslationKey() + '.' + configBase.getNameKey();
    }
    static String fullKeyFromUtilBase(ILPCConfigBase configBase){
        return "lpctools.configs.utils." + configBase.getNameKey();
    }
}
