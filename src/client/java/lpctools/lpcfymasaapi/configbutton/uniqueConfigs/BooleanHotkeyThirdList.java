package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.IThirdListBase;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

//TODO:getAsJsonElement setValueFromJsonElement

//一个有三个按钮：展开，boolean，hotkey的配置
public class BooleanHotkeyThirdList extends LPCConfigBase implements IThirdListBase, IConfigBoolean, IConfigOptionList, IHotkey {
    public final boolean defaultBoolean, defaultExpanded;
    public final @NotNull String defaultHotkeyStorageString;
    private boolean booleanValue;
    private OptionListEnum expanded;
    private final @NotNull IKeybind keybind;
    public final LPCConfigList subConfigs;
    public boolean onValueChangedWhenListCycled;
    public BooleanHotkeyThirdList(@NotNull ILPCConfigList parent, @NotNull String nameKey, boolean defaultBoolean, boolean defaultExpanded, @Nullable String defaultHotkeyStorageString, @Nullable ILPCValueChangeCallback callback, boolean onValueChangedWhenListCycled) {
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
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
        if(isExpanded()) return subConfigs.buildConfigWrappers(wrapperList);
        else return wrapperList;
    }
    @Override protected List<ButtonOption> getButtonOptions() {
        return List.of(
            buttonOptionsPreset(2, this),
            buttonBooleanPreset(1, this),
            buttonKeybindPreset(3, this)
        );
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
    }
    @Override public @NotNull IKeybind getKeybind() {return keybind;}
    public enum OptionListEnum implements IConfigOptionListEntry{
        RETRACTED(false, "lpctools.configs.utils.collapsed"),
        EXPANDED(true, "lpctools.configs.utils.expanded");
        public final boolean expanded;
        public final @NotNull String translationKey;
        OptionListEnum(boolean expanded, @NotNull String translationKey){this.expanded = expanded;this.translationKey = translationKey;}
        @Override public String getStringValue() {return String.valueOf(expanded);}
        @Override public String getDisplayName() {return Text.translatable(translationKey).getString();}
        @Override public OptionListEnum cycle(boolean forward) {return expanded ? RETRACTED : EXPANDED;}
        @Override public OptionListEnum fromString(String value) {
            return get(Boolean.getBoolean(value));
        }
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
}
