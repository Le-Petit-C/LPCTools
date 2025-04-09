package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public abstract class LPCConfig<T extends IConfigBase> implements ILPCConfig{
    @Nullable public IValueRefreshCallback refreshCallback;//值刷新时会调用其中的方法
    public LPCConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, boolean hasHotkey){
        this(defaultParent, nameKey, hasHotkey, null);
    }
    public LPCConfig(@NotNull ILPCConfigList defaultParent, @NotNull String nameKey, boolean hasHotkey, @Nullable IValueRefreshCallback refreshCallback){
        this.parents.add(defaultParent);
        this.nameKey = nameKey;
        this.hasHotkey = hasHotkey;
        this.refreshCallback = refreshCallback;
    }
    @Override public boolean hasHotkey(){return hasHotkey;}
    @Override public void setCallback(IValueRefreshCallback callBack){refreshCallback = callBack;}
    @Override public IValueRefreshCallback getCallback(){return refreshCallback;}
    @Override @NotNull public String getNameKey(){return nameKey;}
    @Override @NotNull public IConfigBase IGetConfig(){return getConfig();}
    @Override public String toString(){return getName();}
    @Override public @NotNull ILPCConfigList getDefaultParent(){return parents.getFirst();}
    @Override public @NotNull Collection<ILPCConfigList> getParents(){return parents;}
    @Override public void addParent(ILPCConfigList parent){parents.add(parent);}
    @Override public IValueRefreshCallback getRefresh(){return refreshCallback;}

    @NotNull protected abstract T createInstance();//创建malilib中的Config实例
    @Nullable protected T getInstance(){return instance;}
    @NotNull protected T getConfig(){
        if(instance == null) {
            instance = createInstance();
            instance.setTranslatedName(getFullNameTranslationKey());
            instance.setComment(getFullCommentTranslationKey());
        }
        return instance;
    }
    protected record LPCConfigCallback<T extends IConfigBase>(@NotNull ILPCConfig parent) implements IValueChangeCallback<T> {
        @Override public void onValueChanged(T config) {parent.callRefresh();}
    }

    @Nullable private T instance;
    @NotNull private final ArrayList<ILPCConfigList> parents = new ArrayList<>();
    @NotNull private final String nameKey;
    private final boolean hasHotkey;
}
