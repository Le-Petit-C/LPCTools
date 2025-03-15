package lpctools.lpcfymasaapi.configbutton;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILPCConfig {
    //获取当前配置所属的配置页
    @NotNull LPCConfigPage getPage();
    //获取当前配置所属的配置列
    @NotNull LPCConfigList getList();
    //获取当前配置本地化键名后缀
    @NotNull String getNameKey();
    //当前配置是否有关热键，决定是否启用热键查找
    boolean hasHotkey();
    //设置当前配置是否显示在列表中
    void setEnabled(boolean enabled);
    //获取当前配置是否显示在列表中
    boolean isEnabled();
    //设置回调函数
    void setCallback(IValueRefreshCallback callBack);
    //获取回调函数
    @Nullable IValueRefreshCallback getCallback();
    //获取IConfigBase，应当在第一次调用时才真正调用malilib中的内容作初始化
    @NotNull IConfigBase IGetConfig();
    //调用刷新方法刷新数据
    void callRefresh();

    //获取当前配置完整本地化键名
    @NotNull default String getFullTranslationKey(){
        return getList().getFullTranslationKey() + ".name." + getNameKey();
    }
    //获取当前配置的本地化键值
    @NotNull default String getName(){
        return StringUtils.translate(getFullTranslationKey());
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
        String key = getNameKey();
        if (!configListJson.has(key)) return;
        IGetConfig().setValueFromJsonElement(configListJson.get(key));
        callRefresh();
    }
}
