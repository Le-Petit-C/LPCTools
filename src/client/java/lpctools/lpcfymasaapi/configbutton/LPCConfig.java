package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;

public interface LPCConfig {
    //应当保证在第一次调用getConfig时才真正调用malilib中的内容作初始化
    IConfigBase getConfig();
    //获取当前配置是否有关热键，决定是否启用热键查找
    boolean hasHotkey();
    //获取当前配置所属的配置页
    LPCConfigPage getPage();
    //获取当前配置所属的配置列
    LPCConfigList getList();
}
