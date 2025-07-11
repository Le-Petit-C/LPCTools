package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.interfaces.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

//一个有三个按钮：展开，boolean，hotkey的配置
public class BooleanHotkeyThirdListConfig extends LPCUniqueConfigBase implements IThirdListBase, IConfigBoolean, IConfigOptionListEx, IHotkey {
    public final boolean defaultBoolean, defaultExpanded;
    public final @NotNull String defaultHotkeyStorageString;
    private boolean booleanValue;
    private OptionListEnum expanded;
    private final @NotNull IKeybind keybind;
    public final LPCConfigList subConfigs;
    public boolean onValueChangedWhenListCycled;
    public BooleanHotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, boolean defaultBoolean, boolean defaultExpanded, @Nullable String defaultHotkeyStorageString, @Nullable ILPCValueChangeCallback callback, boolean onValueChangedWhenListCycled) {
        super(parent, nameKey, callback);
        booleanValue = this.defaultBoolean = defaultBoolean;
        this.defaultExpanded = defaultExpanded;
        expanded = OptionListEnum.get(defaultExpanded);
        this.defaultHotkeyStorageString = defaultHotkeyStorageString == null ? "" : defaultHotkeyStorageString;
        keybind = KeybindMulti.fromStorageString("", KeybindSettings.DEFAULT);
        subConfigs = new LPCConfigList(parent, nameKey);
        this.onValueChangedWhenListCycled = onValueChangedWhenListCycled;
        keybind.setCallback((action, bind)->{toggleBooleanValue(); return true;});
        parent.getPage().getInputHandler().addHotkey(this);
    }
    public BooleanHotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey){
        this(parent, nameKey, false, false, null, null, false);
    }
    public BooleanHotkeyThirdListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        this(parent, nameKey, false, false, null, callback, false);
    }
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
        if(isExpanded()) return subConfigs.buildConfigWrappers(wrapperList);
        else return wrapperList;
    }
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
        res.add(new ButtonOption(-1, this::cycleByMouseButton, null, ILPCUniqueConfigBase.iconButtonAllocator(expanded.icon, LeftRight.CENTER)));
        res.add(ILPCUniqueConfigBase.buttonBooleanPreset(1, this));
        res.add(ILPCUniqueConfigBase.buttonKeybindPreset(3, this));
    }
    @Override public @NotNull Collection<ILPCConfig> getConfigs() {
        return subConfigs.getConfigs();
    }
    @Override public boolean getBooleanValue() {return booleanValue;}
    @Override public boolean getDefaultBooleanValue() {return defaultBoolean;}
    @Override public void setBooleanValue(boolean value) {
        if(value != booleanValue) {
            booleanValue = value;
            onValueChanged();
        }
    }
    @Override public boolean isModified() {
        return booleanValue != defaultBoolean || expanded.expanded != defaultExpanded || !Objects.equals(keybind.getStringValue(), defaultHotkeyStorageString);
    }
    @Override public void resetToDefault() {
        boolean modified = (onValueChangedWhenListCycled && expanded.expanded != defaultExpanded) ||
            booleanValue != defaultBoolean || !Objects.equals(keybind.getStringValue(), defaultHotkeyStorageString);
        booleanValue = defaultBoolean;
        expanded = OptionListEnum.get(defaultExpanded);
        if(modified) onValueChanged();
        getPage().updateIfCurrent();
    }
    @Override public @NotNull IKeybind getKeybind() {return keybind;}
    public enum OptionListEnum implements IConfigOptionListEntry{
        RETRACTED(false, "lpctools.configs.utils.collapsed", MaLiLibIcons.ARROW_DOWN),
        EXPANDED(true, "lpctools.configs.utils.expanded", MaLiLibIcons.ARROW_UP);
        public final boolean expanded;
        public final @NotNull String translationKey;
        public final MaLiLibIcons icon;
        OptionListEnum(boolean expanded, @NotNull String translationKey, MaLiLibIcons icon){
            this.expanded = expanded;this.translationKey = translationKey;this.icon = icon;}
        @Override public String getStringValue() {return String.valueOf(expanded);}
        @Override public String getDisplayName() {return Text.translatable(translationKey).getString();}
        @Override public OptionListEnum cycle(boolean forward) {return expanded ? RETRACTED : EXPANDED;}
        @Override public OptionListEnum fromString(String value) {return get(Boolean.getBoolean(value));}
        public static OptionListEnum get(boolean b){return b ? EXPANDED : RETRACTED;}
    }
    @Override public OptionListEnum getOptionListValue() {
        return expanded;
    }
    @Override public OptionListEnum getDefaultOptionListValue() {
        return OptionListEnum.get(defaultExpanded);
    }
    public boolean isExpanded(){return expanded.expanded;}
    @Override public void setOptionListValue(IConfigOptionListEntry value) {
        if(value instanceof OptionListEnum optionListEnum && optionListEnum != expanded){
            expanded = optionListEnum;
            getPage().updateIfCurrent();
            if(onValueChangedWhenListCycled) onValueChanged();
        }
    }
    
    public final String expandedJsonId = "expanded";
    public final String booleanJsonId = "booleanValue";
    public final String hotkeyJsonId = "hotkey";
    @Override public @Nullable JsonElement getAsJsonElement() {
        JsonObject object = new JsonObject();
        object.add(propertiesId, subConfigs.getAsJsonElement());
        object.addProperty(expandedJsonId, expanded.expanded);
        object.addProperty(booleanJsonId, booleanValue);
        object.add(hotkeyJsonId, keybind.getAsJsonElement());
        return object;
    }
    
    @Override public void setValueFromJsonElement(@NotNull JsonElement data) {
        boolean success = true;
        boolean changed = false;
        if(data instanceof JsonObject object){
            JsonElement list = object.get(propertiesId);
            if(list != null) subConfigs.setValueFromJsonElement(list);
            else success = false;
            JsonElement booleanValueElement = object.get(booleanJsonId);
            if(booleanValueElement instanceof JsonPrimitive primitive){
                boolean newValue = primitive.getAsBoolean();
                if(booleanValue != newValue){
                    booleanValue = newValue;
                    changed = true;
                }
            }
            else success = false;
            JsonElement expandedValueElement = object.get(expandedJsonId);
            if(expandedValueElement instanceof JsonPrimitive primitive){
                OptionListEnum newValue = OptionListEnum.get(primitive.getAsBoolean());
                if(expanded != newValue) {
                    expanded = newValue;
                    if(onValueChangedWhenListCycled)
                        changed = true;
                }
            }
            else success = false;
            JsonElement hotkey = object.get(hotkeyJsonId);
            if(hotkey != null) {
                String lastStorageString = keybind.getStringValue();
                keybind.setValueFromJsonElement(hotkey);
                if(!Objects.equals(lastStorageString, keybind.getStringValue()))
                    changed = true;
            }
            else success = false;
        }
        else success = false;
        if(!success) warnFailedLoadingConfig(this, data);
        if(changed) onValueChanged();
    }
}
