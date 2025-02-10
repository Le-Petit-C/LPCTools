package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;

public interface LPCConfig {
    //应当保证在第一次调用getConfig时才真正调用malilib中的内容作初始化
    IConfigBase getConfig();
}
