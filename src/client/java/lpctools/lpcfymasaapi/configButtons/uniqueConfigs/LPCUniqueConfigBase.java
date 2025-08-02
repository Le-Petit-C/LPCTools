package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import fi.dy.masa.malilib.config.*;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("SameParameterValue")
public abstract class LPCUniqueConfigBase implements ILPCUniqueConfigBase {
    public final @NotNull ILPCConfigReadable parent;
    public final @NotNull String nameKey;
    public @Nullable ILPCValueChangeCallback callback;
    public @NotNull String translatedName = "";
    public @NotNull String comment = "";
    public @NotNull String prettyName;
    public LPCUniqueConfigBase(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        this.parent = parent;
        this.nameKey = nameKey;
        this.callback = callback;
        this.prettyName = nameKey;
    }
    
    @Override public boolean hasHotkey() {return false;}
    @Override public @NotNull ILPCConfigReadable getParent() {return parent;}
    @Override public ConfigType getType() {return null;}
    @Override public String getName() {return nameKey;}
    @Override public @NotNull String getPrettyName() {return prettyName;}
    @Override public @NotNull String getComment() {return comment;}
    @Override public @NotNull String getTranslatedName() {return translatedName;}
    @Override public void setPrettyName(@NotNull String prettyName) {this.prettyName = prettyName;}
    @Override public void setTranslatedName(@NotNull String translatedName) {this.translatedName = translatedName;}
    @Override public void setComment(@NotNull String comment) {this.comment = comment;}
    @Override public void onValueChanged() {
        if(callback != null) callback.onValueChanged();
        LPCConfigPage.ConfigPageInstance instance = getPage().getPageInstance();
        if(instance != null) instance.markConfigsModified();
    }
    @Override public void setValueChangeCallback(@Nullable ILPCValueChangeCallback callback) {
        this.callback = callback;
    }
}
