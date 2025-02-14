package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;

public class BooleanConfig implements LPCConfig {
    //独有功能方法
    public BooleanConfig(LPCConfigList list, String name, boolean defaultBoolean){
        this.list = list;
        this.name = name;
        this.defaultBoolean = defaultBoolean;
    }
    public void setCallBack(IValueChangeCallback<ConfigBoolean> callBack){
        this.callBack = callBack;
        if(instance != null)
            instance.setValueChangeCallback(callBack);
    }
    public BooleanConfig(LPCConfigList list, String name, boolean defaultBoolean, IValueChangeCallback<ConfigBoolean> callback){
        this(list, name, defaultBoolean);
        setCallBack(callback);
    }
    public boolean getValue(){
        if(instance != null)
            return instance.getBooleanValue();
        else return defaultBoolean;
    }
    @Override
    public IConfigBase getConfig(){
        if(instance == null)
            instance = new BooleanConfigInstance(this);
        return instance;
    }

    @Override
    public boolean hasHotkey(){
        return true;
    }

    private static class BooleanConfigInstance extends ConfigBoolean {
        private final BooleanConfig parent;
        public BooleanConfigInstance(BooleanConfig parent){
            super(parent.name, parent.defaultBoolean);
            this.parent = parent;
            apply(parent.list.getFullTranslationKey());
            setValueChangeCallback(parent.callBack);
        }
    }
    private BooleanConfigInstance instance;
    private final LPCConfigList list;
    private final String name;
    private final boolean defaultBoolean;
    private IValueChangeCallback<ConfigBoolean> callBack;

    @Override
    public LPCConfigPage getPage(){
        return list.getPage();
    }

    @Override
    public LPCConfigList getList(){
        return list;
    }
}
