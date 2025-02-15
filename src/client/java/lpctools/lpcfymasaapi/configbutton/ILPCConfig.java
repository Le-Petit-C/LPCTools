package lpctools.lpcfymasaapi.configbutton;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigBase;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ILPCConfig {
    //获取当前配置所属的配置页
    @NotNull LPCConfigPage getPage();
    //获取当前配置所属的配置列
    @NotNull LPCConfigList getList();
    //获取当前配置名
    @NotNull String getName();
    //当前配置是否有关热键，决定是否启用热键查找
    boolean hasHotkey();
    //设置/获取当前配置是否显示在列表中
    void setEnabled(boolean enabled);
    boolean isEnabled();
    //设置回调函数
    void setCallback(IValueRefreshCallback callBack);
    //获取回调函数
    @Nullable IValueRefreshCallback getCallback();
    //从JSON中加载配置
    void setValueFromJsonElement(JsonElement element);
    //获取IConfigBase，应当在第一次调用时才真正调用malilib中的内容作初始化
    @NotNull IConfigBase IGetConfig();
    //调用刷新方法刷新数据
    void callRefresh();
}
