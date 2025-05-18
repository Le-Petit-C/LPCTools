package lpctools.lpcfymasaapi.implementations;

import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;

public interface ILPCConfigBase {
    //获取当前配置的父对象，如果是LPCConfigPage则返回自身
    @NotNull ILPCConfigBase getParent();
    //获取当前项的翻译键
    @NotNull String getNameKey();
    //获取当前配置所属的配置页
    @NotNull LPCConfigPage getPage();
    //获取当前配置的完整本地化键名
    default @NotNull String getFullTranslationKey(){return getFullPath().toString();}
    //一些多用法配置会重载getFullTranslationKey()以重定向到固定的翻译键
    //为了保持三级列表多用法配置内添加其他自定义配置的翻译键仍保持在自定义翻译键下而不是固定的翻译键下
    //使用这个获取根据多级parent定义的而不是配置本身自定义的键
    default @NotNull StringBuilder getFullPath(){
        return getParent().getFullPath().append('.').append(getNameKey());
    }
}
