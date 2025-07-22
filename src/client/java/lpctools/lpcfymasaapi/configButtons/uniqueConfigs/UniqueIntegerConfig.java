package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigInteger;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.interfaces.ISliderCallback;
import fi.dy.masa.malilib.gui.widgets.WidgetSlider;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class UniqueIntegerConfig extends LPCUniqueConfigBase implements IConfigInteger {
    public int intValue, minInteger, maxInteger;
    public final int defaultInteger;
    public boolean useSlider = false;
    public UniqueIntegerConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, int defaultInteger, int minInteger, int maxInteger, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        intValue = this.defaultInteger = defaultInteger;
        this.minInteger = minInteger;
        this.maxInteger = maxInteger;
    }
    public UniqueIntegerConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, int defaultInteger) {
        this(parent, nameKey, defaultInteger, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }
    @Override public int getIntegerValue() {return intValue;}
    @Override public int getDefaultIntegerValue() {return defaultInteger;}
    @Override public void setIntegerValue(int i) {
        if(i < minInteger) i = minInteger;
        else if(i > maxInteger) i = maxInteger;
        if(i != intValue){
            intValue = i;
            onValueChanged();
        }
    }
    @Override public int getMinIntegerValue() {return minInteger;}
    @Override public int getMaxIntegerValue() {return maxInteger;}
    @Override public boolean isModified() {return intValue != defaultInteger;}
    @Override public void resetToDefault() {setIntegerValue(defaultInteger);}
    @Override public String getDefaultStringValue() {return String.valueOf(defaultInteger);}
    @Override public void setValueFromString(String s) {
        try{
            setIntegerValue(Integer.parseInt(s));
        } catch (NumberFormatException ignored){}
    }
    @Override public boolean isModified(String s) {
        try{
            return defaultInteger != Integer.parseInt(s);
        } catch (NumberFormatException ignored){ return true; }
    }
    @Override public String getStringValue() {return String.valueOf(intValue);}
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        if(useSlider){
            res.add(new ButtonOption(1, null, null, (x, y, w, h, str, listener, consumer, resetButton)->{
                ISliderCallback callback = new ISliderCallback() {
                    @Override public int getMaxSteps() {return maxInteger - minInteger > 0 ? Integer.MAX_VALUE : maxInteger - minInteger;}
                    @Override public double getValueRelative() {
                        return ((double)intValue - (double)minInteger) / ((double)maxInteger - (double)minInteger);
                    }
                    @Override public void setValueRelative(double v) {
                        setIntegerValue((int)Math.round(((double)maxInteger - (double)minInteger) * v + minInteger));
                    }
                    @Override public String getFormattedDisplayValue() {return String.valueOf(intValue);}
                };
                WidgetSlider slider = new WidgetSlider(x, y, w, h, callback);
                consumer.addWidget(slider);
            }));
        }
        else res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, this));
        res.add(new ButtonOption(-1, (button, mouseButton)->{
            useSlider = !useSlider;
            getPage().markNeedUpdate();
        }, null, ILPCUniqueConfigBase.iconButtonAllocator( useSlider ? MaLiLibIcons.BTN_TXTFIELD : MaLiLibIcons.BTN_SLIDER, LeftRight.CENTER)));
    }
    @Override public @Nullable JsonPrimitive getAsJsonElement() {return new JsonPrimitive(intValue);}
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        if(element instanceof JsonPrimitive primitive){
            try {
                int lastInt = intValue;
                intValue = primitive.getAsInt();
                return new UpdateTodo().valueChanged(lastInt != intValue);
            } catch (NumberFormatException ignored){}
        }
        warnFailedLoadingConfig(this, element);
        return new UpdateTodo();
    }
}
