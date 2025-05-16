package lpctools.lpcfymasaapi.implementations;

import org.jetbrains.annotations.NotNull;

public interface IThirdListBase extends ILPCConfig, ILPCConfigList{
    @Override default @NotNull String getFullTranslationKey() {
        return ILPCConfig.super.getFullTranslationKey();
    }
}
