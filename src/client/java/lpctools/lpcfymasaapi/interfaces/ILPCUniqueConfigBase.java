package lpctools.lpcfymasaapi.interfaces;

import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.*;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.Supplier;

public interface ILPCUniqueConfigBase extends ILPCUniqueConfig{
    @Override
    default void addButtons(int x, int y, float zLevel, int labelWidth, int configWidth, ButtonConsumer consumer) {
        if(this instanceof IConfigResettable resettable)
            consumer.addButton(consumer.createResetButton(x + configWidth + 2, y, resettable), (button, mouseButton)->resettable.resetToDefault());
        ArrayList<ButtonOption> options = new ArrayList<>();
        getButtonOptions(options);
        float weightSum = 0;
        int h = 20;
        FloatArrayList weightList = new FloatArrayList();
        IntArrayList shiftList = new IntArrayList();
        int shift = 0;
        for(ButtonOption option : options) {
            float weight = option.widthWeight;
            if(weight > 0) weightList.add(weightSum += weight);
            else {
                weightList.add(weightSum);
                shift += (int)-(h * weight);
            }
            shift += 2;
            shiftList.add(shift);
        }
        int weightedWidthSum = configWidth - shift;
        int lastX = x;
        int a = 0;
        for(ButtonOption option : options){
            int currentX = (int)(weightList.getFloat(a) * weightedWidthSum / weightSum) + shiftList.getInt(a) + x;
            IButtonActionListener listener = option.actionListener;
            Supplier<@Nullable String> supplier = option.buttonId;
            IButtonActionListener _listener = (button, mouseButton) -> {
                if(listener != null) listener.actionPerformedWithButton(button, mouseButton);
                String key = supplier == null ? null : supplier.get();
                if(key != null) button.setDisplayString(Text.translatable(key).getString());
            };
            String key = supplier == null ? null : supplier.get();
            String str = key == null ? "" : Text.translatable(key).getString();
            if(option.allocator != null)
                option.allocator.create(lastX, y, currentX - lastX - 2, 20, str, _listener, consumer);
            lastX = currentX;
            ++a;
        }
    }
    
    //allocator为空表示这个位置不创建按钮，只是占位
    //仅在按钮创建时和被按后使用buttonId更新按钮名称，支持翻译键
    //widthWeight为负时表示此配置按钮宽度是相对按钮高度设置的，不再作为宽度占比使用
    record ButtonOption(float widthWeight, @Nullable IButtonActionListener actionListener, @Nullable Supplier<@Nullable String> buttonId, @Nullable IButtonAllocator allocator){}
    void getButtonOptions(ArrayList<ButtonOption> res);
    
    interface IButtonAllocator{
        void create(int x, int y, int w, int h, String str, IButtonActionListener listener, ButtonConsumer consumer);
    }
    //presets
    IButtonAllocator buttonGenericAllocator = (x, y, w, h, key, listener, consumer)->consumer.addButton(new ButtonGeneric(x, y, w, h, key), listener);
    static IButtonAllocator iconButtonAllocator(MaLiLibIcons icon, LeftRight iconAlignment){
        return (x, y, w, h, key, listener, consumer)-> consumer.addButton(new ButtonGeneric(x, y, w, h, key, icon).setIconAlignment(iconAlignment), listener);
    }
    static ButtonOption buttonBooleanPreset(float widthWeight, IConfigBoolean configBoolean){
        return new ButtonOption(widthWeight, null, null,
            (x, y, w, h, key, listener, consumer)->consumer.addButton(new ConfigButtonBoolean(x, y, w, h, configBoolean), listener)
        );
    }
    @SuppressWarnings("unused")
    static ButtonOption buttonOptionsPreset(float weight, IConfigOptionList configOptionList){
        return new ButtonOption(weight, null, null,
            (x, y, w, h, key, listener, consumer)->consumer.addButton(new ConfigButtonOptionList(x, y, w, h, configOptionList), listener)
        );
    }
    static ButtonOption buttonKeybindPreset(float weight, IHotkey hotkey){
        return new ButtonOption(
            weight, null, null,
            (x, y, w, h, key, listener, consumer) -> {
                consumer.addButton(new ConfigButtonKeybind(x, y, w - h - 2, h, hotkey.getKeybind(), consumer.getKeybindHost()), listener);
                consumer.addWidget(new WidgetKeybindSettings(x + w - h, y, h, h, hotkey.getKeybind(), hotkey.getName(), consumer.getWidgetListConfigOptionsBase(), consumer.getKeybindHost().getDialogHandler()));
            }
        );
    }
    @SuppressWarnings("unused")
    static ButtonOption buttonStringListPreset(float weight, IConfigStringList configStringList){
        return new ButtonOption(
            weight, null, null,
            (x, y, w, h, key, listener, consumer) -> consumer.addButton(new ConfigButtonStringList(
                x, y, w, h, configStringList, consumer.getKeybindHost(), consumer.getKeybindHost().getDialogHandler()), listener)
        );
    }
}
