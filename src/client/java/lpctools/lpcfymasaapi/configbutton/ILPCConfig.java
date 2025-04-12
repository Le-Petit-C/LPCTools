package lpctools.lpcfymasaapi.configbutton;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@SuppressWarnings("unused")
public interface ILPCConfig {
    //获取当前配置本地化键名后缀
    @NotNull String getNameKey();
    //当前配置是否有关热键，决定是否启用热键查找
    boolean hasHotkey();
    //设置回调函数
    void setCallback(IValueRefreshCallback callBack);
    //获取回调函数
    @Nullable IValueRefreshCallback getCallback();
    //获取IConfigBase，应当在第一次调用时才真正调用malilib中的内容作初始化
    @NotNull IConfigBase IGetConfig();
    //添加parent
    void addParent(ILPCConfigList parent);
    //获取默认parent，所有config都应该有至少一个parent用于生成translation key
    @NotNull ILPCConfigList getDefaultParent();
    //获取所有parents
    @NotNull Collection<ILPCConfigList> getParents();
    //获取刷新方法对象
    @Nullable IValueRefreshCallback getRefresh();

    default void refreshName(boolean align){
        IConfigBase config = IGetConfig();
        config.setTranslatedName(align ? getAlignedName() : getName());
    }
    //调用刷新方法刷新数据
    default void callRefresh(){
        IValueRefreshCallback callback = getRefresh();
        if(callback != null) callback.valueRefreshCallback();
    }
    //获取当前配置名称的完整本地化键名
    @NotNull default String getFullNameTranslationKey(){
        return getDefaultParent().getFullTranslationKey() + "." + getNameKey()+ ".name";
    }
    //获取当前配置注解的完整本地化键名
    @NotNull default String getFullCommentTranslationKey(){
        return getDefaultParent().getFullTranslationKey() + "." + getNameKey() + ".comment";
    }
    //获取当前配置的本地化键值
    @NotNull default String getName(){
        return StringUtils.translate(getFullNameTranslationKey());
    }
    //获取当前配置基于默认parent对齐后的本地化键值
    @NotNull default String getAlignedName(){
        StringBuilder result = new StringBuilder();
        ILPCConfigList parent = getDefaultParent();
        while(parent instanceof ILPCConfig config){
            result.append("    ");
            parent = config.getDefaultParent();
        }
        result.append(getName());
        return result.toString();
    }
    //从JSON中加载配置
    @NotNull default JsonElement getAsJsonElement() {
        return IGetConfig().getAsJsonElement();
    }
    //转化为JSON加入到配置列表JSON中
    default void addIntoConfigListJson(@NotNull JsonObject configListJson){
        configListJson.add(getNameKey(), getAsJsonElement());
    }
    //从配置列表JSON中加载配置
    default void loadFromConfigListJson(@NotNull JsonObject configListJson){
        if (!configListJson.has(getNameKey())) return;
        IGetConfig().setValueFromJsonElement(configListJson.get(getNameKey()));
        callRefresh();
    }
}
