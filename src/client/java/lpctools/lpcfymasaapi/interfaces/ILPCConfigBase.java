package lpctools.lpcfymasaapi.interfaces;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    //保存和加载时用的内容
    @Nullable JsonElement getAsJsonElement();
    default void addIntoParentJsonObject(@NotNull JsonObject object){
        object.add(getNameKey(), getAsJsonElement());
    }
    void setValueFromJsonElement(@NotNull JsonElement data);
    default void setValueFromParentJsonObject(@NotNull JsonObject object){
        setValueFromJsonElement(object.get(getNameKey()));
    }
    //获取自己的子配置需要对齐的空格数
    default String getAlignSpaces(){
        return getParent().getAlignSpaces() + "    ";
    }
    //获取自己需要对齐的空格数
    default String getParentSpaces(){
        return getParent().getAlignSpaces();
    }
}
