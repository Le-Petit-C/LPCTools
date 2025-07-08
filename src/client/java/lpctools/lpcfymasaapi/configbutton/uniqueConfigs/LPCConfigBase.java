package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigOptionList;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.button.*;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import lpctools.lpcfymasaapi.interfaces.ButtonConsumer;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("SameParameterValue")
public abstract class LPCConfigBase implements ILPCUniqueConfig {
    public final @NotNull ILPCConfigList parent;
    public final @NotNull String nameKey;
    public @Nullable ILPCValueChangeCallback callback;
    public @NotNull String translatedName;
    public @NotNull String comment;
    public @NotNull String prettyName;
    public LPCConfigBase(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        this.parent = parent;
        this.nameKey = nameKey;
        this.callback = callback;
        this.translatedName = getFullNameTranslationKey();
        this.comment = getFullCommentTranslationKey();
        this.prettyName = nameKey;
    }
    
    @Override
    public void addButtons(int x, int y, float zLevel, int labelWidth, int configWidth, ButtonConsumer consumer) {
        if(this instanceof IConfigResettable resettable)
            consumer.addButton(consumer.createResetButton(x + configWidth + 2, y, resettable), (button, mouseButton)->resettable.resetToDefault());
        List<ButtonOption> options = getButtonOptions();
        float weightSum = 0;
        FloatArrayList weightList = new FloatArrayList();
        for(ButtonOption option : options) weightList.add(weightSum += option.widthWeight);
        int size = options.size();
        int buttonWidthSum = configWidth - 2 * (size - 1);
        int lastX = x;
        for(int a = 0; a < size; ++a){
            int currentX = (int)(weightList.getFloat(a) * buttonWidthSum / weightSum) + 2 * a + x;
            ButtonOption option = options.get(a);
            IButtonActionListener listener = option.actionListener;
            Supplier<@Nullable String> supplier = option.buttonId;
            String _key = supplier == null ? null : supplier.get();
            String translationKey = _key == null ? "" : _key;
            IButtonActionListener _listener = (button, mouseButton) -> {
                if(listener != null) listener.actionPerformedWithButton(button, mouseButton);
                String key = supplier == null ? null : supplier.get();
                if(key != null) button.setDisplayString(Text.translatable(key).getString());
            };
            if(option.allocator != null)
                option.allocator.create(lastX, y, currentX - lastX, translationKey, _listener, consumer);
            lastX = currentX + 2;
        }
    }
    
    @Override public boolean hasHotkey() {return false;}
    @Override public @NotNull ILPCConfigList getParent() {return parent;}
    @Override public ConfigType getType() {return null;}
    @Override public String getName() {return nameKey;}
    @Override public @NotNull String getPrettyName() {return prettyName;}
    @Override public @NotNull String getComment() {return comment;}
    @Override public @NotNull String getTranslatedName() {return translatedName;}
    @Override public void setPrettyName(@NotNull String prettyName) {this.prettyName = prettyName;}
    @Override public void setTranslatedName(@NotNull String translatedName) {this.translatedName = translatedName;}
    @Override public void setComment(@NotNull String comment) {this.comment = comment;}
    @Override public void onValueChanged() {
        if(callback == null) return;
        callback.onValueChanged();
    }
    @Override public void setValueChangeCallback(@Nullable ILPCValueChangeCallback callback) {
        this.callback = callback;
    }
    
    protected interface IButtonAllocator{
        void create(int x, int y, int w, String translationKey, IButtonActionListener listener, ButtonConsumer consumer);
    }
    //allocator为空表示这个位置不创建按钮，只是占位
    //仅在按钮创建时和被按后使用buttonId更新按钮名称，支持翻译键
    protected record ButtonOption(float widthWeight, @Nullable IButtonActionListener actionListener, @Nullable Supplier<@Nullable String> buttonId, @Nullable IButtonAllocator allocator){}
    protected abstract List<ButtonOption> getButtonOptions();
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
                return LPCConfigBase.this.getName();
            }
            @Override public String getComment() {
                return LPCConfigBase.this.getComment();
            }
            @Override public String getTranslatedName() {
                return LPCConfigBase.this.getTranslatedName();
            }
            @Override public void setPrettyName(String prettyName) {
                LPCConfigBase.this.setPrettyName(prettyName);
            }
            @Override public void setTranslatedName(String translatedName) {
                LPCConfigBase.this.setTranslatedName(translatedName);
            }
            @Override public void setComment(String comment) {
                LPCConfigBase.this.setComment(comment);
            }
            @Override public void setValueFromJsonElement(JsonElement element) {
                keybind.setValueFromJsonElement(element);
            }
            @Override public JsonElement getAsJsonElement() {
                return keybind.getAsJsonElement();
            }
        };
    }
    //presets
    protected static IButtonAllocator buttonGenericAllocator = (x, y, w, key, listener, consumer)->consumer.addButton(x, y, w, false, key, listener);
    protected static ButtonOption buttonBooleanPreset(float widthWeight, IConfigBoolean configBoolean){
        return new ButtonOption(widthWeight, null, null,
            (x, y, w, key, listener, consumer)->consumer.addButton(new ConfigButtonBoolean(x, y, w, 20, configBoolean), listener)
        );
    }
    protected static ButtonOption buttonOptionsPreset(float widthWeight, IConfigOptionList configOptionList){
        return new ButtonOption(widthWeight, null, null,
            (x, y, w, key, listener, consumer)->consumer.addButton(new ConfigButtonOptionList(x, y, w, 20, configOptionList), listener)
        );
    }
    protected static ButtonOption buttonKeybindPreset(float weight, IHotkey hotkey){
        return new ButtonOption(
            weight, null, null,
            (x, y, w, key, listener, consumer) -> {
                consumer.addButton(new ConfigButtonKeybind(x, y, w - 22, 20, hotkey.getKeybind(), consumer.getKeybindHost()), listener);
                consumer.addWidget(new WidgetKeybindSettings(x + w - 20, y, 20, 20, hotkey.getKeybind(), hotkey.getName(), consumer.getWidgetListConfigOptionsBase(), consumer.getKeybindHost().getDialogHandler()));
            }
        );
    }
}
