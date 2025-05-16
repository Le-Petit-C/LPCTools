package lpctools.lpcfymasaapi.implementations;

import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;

public interface ILPCConfigBase {
    //获取当前项的翻译键
    @NotNull String getNameKey();
    //获取当前配置的完整本地化键名
    @NotNull String getFullTranslationKey();
    //获取当前配置所属的配置页
    @NotNull LPCConfigPage getPage();
}
