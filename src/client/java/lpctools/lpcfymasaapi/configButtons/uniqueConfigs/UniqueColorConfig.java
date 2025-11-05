package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigColor;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class UniqueColorConfig extends LPCUniqueConfigBase implements IConfigColor {
    private Color4f color;
    protected Color4f defaultColor;
    
    public UniqueColorConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Color4f defaultColor, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        this.color = this.defaultColor = defaultColor;
    }
    public UniqueColorConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, int defaultColor, @Nullable ILPCValueChangeCallback callback) {
        this(parent, nameKey, Color4f.fromColor(defaultColor), callback);
    }
    public UniqueColorConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, int defaultColor) {
        this(parent, nameKey, defaultColor, null);
    }
    
    @Override public Color4f getColor() {return color;}
    public void setColor(Color4f color) {
        if(!color.equals(this.color)){
            this.color = color;
            onValueChanged();
        }
    }
    @Override public int getIntegerValue() {return color.intValue;}
    @Override public int getDefaultIntegerValue() {return defaultColor.intValue;}
    @Override public void setIntegerValue(int i) {
        if(i != color.intValue){
            color = Color4f.fromColor(color);
            onValueChanged();
        }
    }
    @Override public int getMinIntegerValue() {return Integer.MIN_VALUE;}
    @Override public int getMaxIntegerValue() {return Integer.MAX_VALUE;}
    @Override public boolean isModified() {return color.intValue != defaultColor.intValue;}
    @Override public void resetToDefault() {color = defaultColor;}
    @Override public String getDefaultStringValue() {return defaultColor.toString();}
    @Override public void setValueFromString(String s) {
        var color = Color4f.fromString(s);
        setColor(color);
    }
    @Override public boolean isModified(String s) {
        return Color4f.fromString(s).equals(defaultColor);
    }
    @Override public String getStringValue() {return color.toString();}
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, this));
        res.add(new ButtonOption(-1, null, null, ILPCUniqueConfigBase.colorEditorAllocator(this)));
    }
    public static JsonPrimitive getUniqueColorConfigAsJsonElement(UniqueColorConfig config) {return new JsonPrimitive(config.getStringValue());}
    @Override public @Nullable JsonElement getAsJsonElement() {return getUniqueColorConfigAsJsonElement(this);}
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        if(element instanceof JsonPrimitive primitive)
            setValueFromString(primitive.getAsString());
        else warnFailedLoadingConfig(this, element);
        return new UpdateTodo();
    }
}
