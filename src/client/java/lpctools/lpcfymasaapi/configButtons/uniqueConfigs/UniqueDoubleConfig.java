package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigDouble;
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

public class UniqueDoubleConfig extends LPCUniqueConfigBase implements IConfigDouble {
    public double doubleValue, minDouble, maxDouble;
    public final double defaultDouble;
    public boolean useSlider = false;
    public UniqueDoubleConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, double defaultDouble, double minDouble, double maxDouble, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        doubleValue = this.defaultDouble = defaultDouble;
        this.minDouble = minDouble;
        this.maxDouble = maxDouble;
    }
    public UniqueDoubleConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, int defaultDouble) {
        this(parent, nameKey, defaultDouble, -Double.MAX_VALUE, Double.MAX_VALUE, null);
    }
    @Override public double getDoubleValue() {return doubleValue;}
    @Override public double getDefaultDoubleValue() {return defaultDouble;}
    @Override public void setDoubleValue(double d) {
        if(d < minDouble) d = minDouble;
        else if(d > maxDouble) d = maxDouble;
        if(d != doubleValue){
            doubleValue = d;
            onValueChanged();
        }
    }
    @Override public double getMinDoubleValue() {return minDouble;}
    @Override public double getMaxDoubleValue() {return maxDouble;}
    @Override public boolean isModified() {return doubleValue != defaultDouble;}
    @Override public void resetToDefault() {setDoubleValue(defaultDouble);}
    @Override public String getDefaultStringValue() {return String.valueOf(defaultDouble);}
    @Override public void setValueFromString(String s) {
        try{
            setDoubleValue(Double.parseDouble(s));
        } catch (NumberFormatException ignored){}
    }
    @Override public boolean isModified(String s) {
        try{
            return defaultDouble != Double.parseDouble(s);
        } catch (NumberFormatException ignored){ return true; }
    }
    @Override public String getStringValue() {return String.valueOf(doubleValue);}
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        if(useSlider){
            res.add(new ButtonOption(1, null, null, (x, y, w, h, str, listener, consumer, resetButton)->{
                ISliderCallback callback = new ISliderCallback() {
                    @Override public int getMaxSteps() {return Integer.MAX_VALUE;}
                    @Override public double getValueRelative() {
                        return (doubleValue / 2 - minDouble / 2) / (maxDouble / 2 - minDouble / 2);
                    }
                    @Override public void setValueRelative(double v) {
                        setDoubleValue(maxDouble * v + minDouble * (1 - v));
                    }
                    @Override public String getFormattedDisplayValue() {return String.valueOf(doubleValue);}
                };
                WidgetSlider slider = new WidgetSlider(x, y, w, h, callback);
                consumer.addWidget(slider);
            }));
        }
        else res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, this));
        res.add(ILPCUniqueConfigBase.iconButtonPreset(useSlider ? MaLiLibIcons.BTN_TXTFIELD : MaLiLibIcons.BTN_SLIDER,
            (button, mouseButton)->{useSlider = !useSlider;getPage().markNeedUpdate();}, null));
    }
    @Override public @Nullable JsonPrimitive getAsJsonElement() {return new JsonPrimitive(doubleValue);}
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        if(element instanceof JsonPrimitive primitive){
            try {
                double lastDouble = doubleValue;
                doubleValue = primitive.getAsDouble();
                return new UpdateTodo().valueChanged(lastDouble != doubleValue);
            } catch (NumberFormatException ignored){
                warnFailedLoadingConfig(this, element);
            }
        }
        warnFailedLoadingConfig(this, element);
        return new UpdateTodo();
    }
}
