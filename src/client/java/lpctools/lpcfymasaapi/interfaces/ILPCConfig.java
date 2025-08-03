package lpctools.lpcfymasaapi.interfaces;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigBase;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import org.jetbrains.annotations.NotNull;

import static lpctools.generic.GenericConfigs.*;

public interface ILPCConfig extends ILPCConfigBase, IConfigBase, ILPCConfigNotifiable {
    //当前配置是否有关热键，决定是否启用热键查找
    boolean hasHotkey();
    String getTranslatedName();
    void setTranslatedName(String name);
    void setComment(String comment);

    @Override default @NotNull String getNameKey(){return getName();}
    @Override default @NotNull LPCConfigPage getPage(){return getParent().getPage();}
    @Override default void setValueFromJsonElement(@NotNull JsonElement data){
        UpdateTodo todo = setValueFromJsonElementEx(data);
        if(todo.valueChanged) onValueChanged();
    }
    
    @Override default String getConfigGuiDisplayName() {
        return getTranslatedName();
    }
    //根据是否对齐刷新当前配置名
    default void refreshName(){
        setTranslatedName(getAlignedNameTranslation());
        setComment(getFullCommentTranslationKey());
    }
    //获取当前配置基于默认parent对齐后的配置名
    default @NotNull String getAlignedNameTranslation(){
        return " ".repeat(indentSpaces.getAsInt() * getAlignLevel()) + getNameTranslation();
    }
}
