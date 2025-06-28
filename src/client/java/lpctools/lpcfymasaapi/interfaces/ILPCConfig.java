package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import lpctools.lpcfymasaapi.LPCConfigPage;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface ILPCConfig extends ILPCConfigBase, IConfigBase, IConfigResettable, ILPCConfigNotifiable {
    //当前配置是否有关热键，决定是否启用热键查找
    boolean hasHotkey();
    String getTranslatedName();
    void setTranslatedName(String name);
    
    @Override @NotNull ILPCConfigList getParent();

    @Override default @NotNull String getNameKey(){return getName();}
    @Override default @NotNull LPCConfigPage getPage(){return getParent().getPage();}
    @Override default String getConfigGuiDisplayName() {
        return getTranslatedName();
    }
    //根据是否对齐刷新当前配置名
    default void refreshName(boolean align){
        setTranslatedName(align ? getAlignedNameTranslation() : getNameTranslation());
    }
    //获取当前配置名称的完整本地化键名
    default @NotNull String getFullNameTranslationKey(){
        return getFullTranslationKey() + ".name";
    }
    //获取当前配置注解的完整本地化键名
    default @NotNull String getFullCommentTranslationKey(){
        return getFullTranslationKey() + ".comment";
    }
    //获取当前配置名
    default @NotNull String getNameTranslation(){
        return Text.translatable(getFullNameTranslationKey()).getString();
    }
    //获取当前配置基于默认parent对齐后的配置名
    default @NotNull String getAlignedNameTranslation(){
        return getParentSpaces() + getNameTranslation();
    }
}
