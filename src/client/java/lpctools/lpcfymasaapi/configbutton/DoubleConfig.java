package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;

public class DoubleConfig extends LPCConfig{
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble){
        this.list = list;
        this.name = name;
        this.defaultDouble = defaultDouble;
    }
    public void setCallBack(IValueChangeCallback<ConfigDouble> callBack){
        this.callBack = callBack;
        if(instance != null)
            instance.setValueChangeCallback(callBack);
    }
    public DoubleConfig(LPCConfigList list, String name, double defaultDouble, IValueChangeCallback<ConfigDouble> callback){
        this(list, name, defaultDouble);
        setCallBack(callback);
    }
    public double getValue(){
        if(instance != null)
            return instance.getDoubleValue();
        else return defaultDouble;
    }
    @Override
    public IConfigBase getConfig(){
        if(instance == null)
            instance = new DoubleConfigInstance(this);
        return instance;
    }

    @Override
    public boolean hasHotkey(){
        return true;
    }

    private static class DoubleConfigInstance extends ConfigDouble {
        private final DoubleConfig parent;
        public DoubleConfigInstance(DoubleConfig parent){
            super(parent.name, parent.defaultDouble);
            this.parent = parent;
            apply(parent.list.getFullTranslationKey());
            setValueChangeCallback(parent.callBack);
        }
    }
    private DoubleConfigInstance instance;
    private final LPCConfigList list;
    private final String name;
    private final double defaultDouble;
    private IValueChangeCallback<ConfigDouble> callBack;

    @Override
    public LPCConfigPage getPage(){
        return list.getPage();
    }

    @Override
    public LPCConfigList getList(){
        return list;
    }
}
