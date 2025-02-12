package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;

public class BooleanHotkeyConfig implements LPCConfig {
    //独有功能方法
    public BooleanHotkeyConfig(LPCConfigList list, String name, boolean defaultBoolean, String defaultStorageString){
        this.list = list;
        this.name = name;
        this.defaultBoolean = defaultBoolean;
        this.defaultStorageString = defaultStorageString;
    }
    public void setCallBack(IValueChangeCallback<ConfigBoolean> callback){
        this.callback = callback;
        if(instance != null) instance.setValueChangeCallback(callback);
    }
    public BooleanHotkeyConfig(LPCConfigList list, String name, boolean defaultBoolean, String defaultStorageString, IValueChangeCallback<ConfigBoolean> callback){
        this(list, name, defaultBoolean, defaultStorageString);
        setCallBack(callback);
    }

    //接口重载函数
    @Override
    public IConfigBase getConfig(){
        if(instance == null)
            instance = new BooleanHotkeyConfigInstance(this);
        return instance;
    }

    @Override
    public boolean hasHotkey(){
        return true;
    }

    private static class BooleanHotkeyConfigInstance extends ConfigBooleanHotkeyed {
        private final BooleanHotkeyConfig parent;
        public BooleanHotkeyConfigInstance(BooleanHotkeyConfig parent){
            super(parent.name, parent.defaultBoolean, parent.defaultStorageString);
            this.parent = parent;
            apply(parent.list.getFullTranslationKey());
            parent.list.getPage().getInputHandler().addHotkey(this);
            setValueChangeCallback(parent.callback);
        }
    }
    private BooleanHotkeyConfigInstance instance;
    private final LPCConfigList list;
    private final String name;
    private final boolean defaultBoolean;
    private final String defaultStorageString;
    private IValueChangeCallback<ConfigBoolean> callback;

    @Override
    public LPCConfigPage getPage(){
        return list.getPage();
    }

    @Override
    public LPCConfigList getList(){
        return list;
    }
}
