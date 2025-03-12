package lpctools.lpcfymasaapi.configbutton;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.LPCConfigPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LPCConfig<T extends IConfigBase> implements ILPCConfig{
    @NotNull public final LPCConfigList list;
    @NotNull public final String nameKey;
    public final boolean hasHotkey;
    public boolean enabled = true;
    @Nullable public IValueRefreshCallback refreshCallback;//值刷新时会调用其中的方法
    public LPCConfig(@NotNull LPCConfigList list, @NotNull String nameKey, boolean hasHotkey){
        this.list = list;
        this.nameKey = nameKey;
        this.hasHotkey = hasHotkey;
    }
    @Override @NotNull public LPCConfigPage getPage(){return list.getPage();}
    @Override @NotNull public LPCConfigList getList(){return list;}
    @Override public boolean hasHotkey(){return hasHotkey;}
    @Override public void setEnabled(boolean enabled){this.enabled = enabled;}
    @Override public boolean isEnabled(){return enabled;}
    @Override public void setCallback(IValueRefreshCallback callBack){refreshCallback = callBack;}
    @Override public IValueRefreshCallback getCallback(){return refreshCallback;}
    @Override @NotNull public String getNameKey(){return nameKey;}
    @Override @NotNull public String getName(){
        return StringUtils.translate(list.getFullTranslationKey() + ".name." + getNameKey());
    }
    @Override public void setValueFromJsonElement(JsonElement element){
        getConfig().setValueFromJsonElement(element);
        callRefresh();
    }
    @Override @NotNull public IConfigBase IGetConfig(){return getConfig();}
    @Override public void callRefresh(){if(refreshCallback != null) refreshCallback.valueRefreshCallback();}

    @NotNull protected abstract T createInstance();//创建malilib中的Config实例
    @Nullable protected T getInstance(){return instance;}
    @NotNull protected T getConfig(){
        if(instance == null) instance = createInstance();
        return instance;
    }
    protected record LPCConfigCallback<T extends IConfigBase>(@NotNull ILPCConfig parent) implements IValueChangeCallback<T> {
        @Override public void onValueChanged(T config) {parent.callRefresh();}
    }

    @Nullable private T instance;
}
