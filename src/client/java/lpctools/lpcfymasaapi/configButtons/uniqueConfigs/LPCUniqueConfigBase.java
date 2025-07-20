package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("SameParameterValue")
public abstract class LPCUniqueConfigBase implements ILPCUniqueConfigBase {
    public final @NotNull ILPCConfigBase parent;
    public final @NotNull String nameKey;
    public @Nullable ILPCValueChangeCallback callback;
    public @NotNull String translatedName = "";
    public @NotNull String comment = "";
    public @NotNull String prettyName;
    public LPCUniqueConfigBase(@NotNull ILPCConfigBase parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        this.parent = parent;
        this.nameKey = nameKey;
        this.callback = callback;
        this.prettyName = nameKey;
    }
    
    @SuppressWarnings("unused")
    public IHotkey createHotkey(@Nullable String defaultStorageString, KeybindSettings settings){
        return new IHotkey() {
            final KeybindMulti keybind = KeybindMulti.fromStorageString(
                defaultStorageString == null ? "" : defaultStorageString, settings);
            @Override public IKeybind getKeybind() {
                return keybind;
            }
            @Override public ConfigType getType() {
                return ConfigType.HOTKEY;
            }
            @Override public String getName() {
                return LPCUniqueConfigBase.this.getName();
            }
            @Override public String getComment() {
                return LPCUniqueConfigBase.this.getComment();
            }
            @Override public String getTranslatedName() {
                return LPCUniqueConfigBase.this.getTranslatedName();
            }
            @Override public void setPrettyName(String prettyName) {
                LPCUniqueConfigBase.this.setPrettyName(prettyName);
            }
            @Override public void setTranslatedName(String translatedName) {
                LPCUniqueConfigBase.this.setTranslatedName(translatedName);
            }
            @Override public void setComment(String comment) {
                LPCUniqueConfigBase.this.setComment(comment);
            }
            @Override public void setValueFromJsonElement(JsonElement element) {
                keybind.setValueFromJsonElement(element);
            }
            @Override public JsonElement getAsJsonElement() {
                return keybind.getAsJsonElement();
            }
        };
    }
    
    @Override public boolean hasHotkey() {return false;}
    @Override public @NotNull ILPCConfigBase getParent() {return parent;}
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
