package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import lpctools.lpcfymasaapi.LPCConfigPage;

public class HotkeyConfig implements LPCConfig {
    //独有功能方法
    public HotkeyConfig(LPCConfigPage page, String name, String defaultStorageString, String translationPrefix){
        this.page = page;
        this.name = name;
        this.defaultStorageString = defaultStorageString;
        this.translationPrefix = page.getModReference().modId + ".configs." + translationPrefix;
    }
    public void setCallBack(IHotkeyCallback callBack){
        this.callBack = callBack;
    }
    public HotkeyConfig(LPCConfigPage page, String name, String defaultStorageString, String translationPrefix, IHotkeyCallback callBack){
        this(page, name, defaultStorageString, translationPrefix);
        setCallBack(callBack);
    }

    //接口重载函数
    @Override
    public IConfigBase getConfig(){
        if(instance == null)
            instance = new HotkeyConfigInstance(this, name, defaultStorageString, translationPrefix);
        return instance;
    }

    private static class HotkeyConfigInstance extends ConfigHotkey {
        HotkeyConfig parent;
        public HotkeyConfigInstance(HotkeyConfig parent, String name, String defaultStorageString, String translationPrefix){
            super(name, defaultStorageString);
            this.parent = parent;
            apply(translationPrefix);
            getKeybind().setCallback(parent.callBack);
            parent.page.getInputHandler().addHotkey(this);
        }
    }
    private HotkeyConfigInstance instance;
    private final LPCConfigPage page;
    private final String name;
    private final String defaultStorageString;
    private final String translationPrefix;
    private IHotkeyCallback callBack;
    public LPCConfigPage getPage(){
        return page;
    }
}
