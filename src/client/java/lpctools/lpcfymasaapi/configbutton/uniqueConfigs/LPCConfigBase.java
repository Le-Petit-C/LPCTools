package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.ConfigType;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
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
            Supplier<String> supplier = option.buttonId;
            String translationKey = supplier.get();
            if(listener != null) consumer.addButton(lastX, y, currentX - lastX, false, translationKey,
                (button, mouseButton) -> {
                    listener.actionPerformedWithButton(button, mouseButton);
                    button.setDisplayString(Text.translatable(supplier.get()).getString());
                });
            lastX = currentX + 2;
        }
    }
    
    @Override public boolean hasHotkey() {return false;}
    @Override public @NotNull ILPCConfigList getParent() {return parent;}
    @Override public @Nullable JsonElement getAsJsonElement() {return null;}
    @Override public void setValueFromJsonElement(@NotNull JsonElement data) {}
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
    
    //actionListener为空表示这个位置不创建按钮，只是占位
    //仅在按钮创建时和被按后使用buttonId更新按钮名称，支持翻译键
    protected record ButtonOption(float widthWeight, @Nullable IButtonActionListener actionListener, Supplier<@Nullable String> buttonId){}
    protected abstract List<ButtonOption> getButtonOptions();
}
