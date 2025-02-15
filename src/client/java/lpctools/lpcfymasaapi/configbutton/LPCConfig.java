package lpctools.lpcfymasaapi.configbutton;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigBase;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;

public abstract class LPCConfig {
    public boolean enabled = true;
    //应当保证在第一次调用getConfig时才真正调用malilib中的内容作初始化
    public abstract IConfigBase getConfig();
    //获取当前配置是否有关热键，决定是否启用热键查找
    public abstract boolean hasHotkey();
    //获取当前配置所属的配置页
    public abstract LPCConfigPage getPage();
    //获取当前配置所属的配置列
    public abstract LPCConfigList getList();
    public String getName(){return getConfig().getName();}
    public void setValueFromJsonElement(JsonElement element){getConfig().setValueFromJsonElement(element);}
}
