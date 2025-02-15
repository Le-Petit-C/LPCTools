package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;

public class HotkeyConfig extends LPCConfig {
    public HotkeyConfig(LPCConfigList list, String name, String defaultStorageString){
        this.list = list;
        this.name = name;
        this.defaultStorageString = defaultStorageString;
    }
    public void setCallBack(IHotkeyCallback callBack){
        this.callBack = callBack;
    }
    public HotkeyConfig(LPCConfigList list, String name, String defaultStorageString, IHotkeyCallback callBack){
        this(list, name, defaultStorageString);
        setCallBack(callBack);
    }

    //接口重载函数
    @Override
    public IConfigBase getConfig(){
        if(instance == null)
            instance = new HotkeyConfigInstance(this);
        return instance;
    }

    @Override
    public boolean hasHotkey(){
        return true;
    }

    private static class HotkeyConfigInstance extends ConfigHotkey {
        HotkeyConfig parent;
        public HotkeyConfigInstance(HotkeyConfig parent){
            super(parent.name, parent.defaultStorageString);
            this.parent = parent;
            apply(parent.list.getFullTranslationKey());
            getKeybind().setCallback(parent.callBack);
            parent.list.getPage().getInputHandler().addHotkey(this);
        }
    }
    private HotkeyConfigInstance instance;
    private final LPCConfigList list;
    private final String name;
    private final String defaultStorageString;
    private IHotkeyCallback callBack;

    @Override
    public LPCConfigPage getPage(){
        return list.getPage();
    }

    @Override
    public LPCConfigList getList(){
        return list;
    }
}
