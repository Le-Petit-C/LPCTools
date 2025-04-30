package lpctools.lpcfymasaapi.configbutton;

import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILPCConfig extends IConfigBase, IConfigResettable, ILPCConfigNotifiable {
    record Data(@NotNull ILPCConfigList parent, boolean hasHotkey) {}
    //获取data
    @NotNull Data getLPCConfigData();
    //默认初始化，应在getLPCConfigData有效化后调用
    default void ILPC_MASAConfigWrapperDefaultInit(@Nullable ILPCValueChangeCallback callback){
        setComment(getFullCommentTranslationKey());
        setValueChangeCallback(callback);
    }
    //当前配置是否有关热键，决定是否启用热键查找
    default boolean hasHotkey(){return getLPCConfigData().hasHotkey;}
    //获取parent
    default @NotNull ILPCConfigList getParent(){return getLPCConfigData().parent;}

    default void refreshName(boolean align){setTranslatedName(align ? getAlignedNameTranslation() : getNameTranslation());}

    //获取当前配置名称的完整本地化键名
    @NotNull default String getFullNameTranslationKey(){
        return getParent().getFullTranslationKey() + "." + getName()+ ".name";
    }
    //获取当前配置注解的完整本地化键名
    @NotNull default String getFullCommentTranslationKey(){
        return getParent().getFullTranslationKey() + "." + getName() + ".comment";
    }
    //获取当前配置基于默认parent对齐后的本地化键值
    @NotNull default String getAlignedNameTranslation(){
        StringBuilder result = new StringBuilder();
        ILPCConfigList parent = getParent();
        while(parent instanceof ILPCConfig config){
            result.append("    ");
            parent = config.getParent();
        }
        result.append(getNameTranslation());
        return result.toString();
    }
    @NotNull default String getNameTranslation(){
        return Text.translatable(getFullNameTranslationKey()).getString();
    }
    //转化为JSON加入到配置列表JSON中
    default void addIntoConfigListJson(@NotNull JsonObject configListJson){
        configListJson.add(getName(), getAsJsonElement());
    }
    //从配置列表JSON中加载配置
    default void loadFromConfigListJson(@NotNull JsonObject configListJson){
        if (!configListJson.has(getName())) return;
        setValueFromJsonElement(configListJson.get(getName()));
        onValueChanged();
    }
}
