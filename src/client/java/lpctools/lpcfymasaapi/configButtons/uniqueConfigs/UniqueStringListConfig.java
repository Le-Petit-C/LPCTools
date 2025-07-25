package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IConfigStringList;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class UniqueStringListConfig extends LPCUniqueConfigBase implements IConfigStringList, IConfigResettable {
    public final ImmutableList<String> defaultStrings;
    public final ArrayList<String> strings = new ArrayList<>();
    public UniqueStringListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ImmutableList<String> defaultStrings) {
        this(parent, nameKey, defaultStrings, null);
    }
    public UniqueStringListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ImmutableList<String> defaultStrings, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        this.defaultStrings = defaultStrings == null ? ImmutableList.of() : defaultStrings;
        strings.addAll(this.defaultStrings);
    }
    @Override public List<String> getStrings() {return strings;}
    @Override public ImmutableList<String> getDefaultStrings() {return defaultStrings;}
    @Override public void setStrings(List<String> strings) {
        if(!strings.equals(this.strings)){
            this.strings.clear();
            this.strings.addAll(strings);
            onValueChanged();
        }
    }
    @Override public void setModified() {onValueChanged();}
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        res.add(ILPCUniqueConfigBase.buttonStringListPreset(1, this));
    }
    @Override public @Nullable JsonElement getAsJsonElement() {
        JsonArray array = new JsonArray();
        for(String string : strings) array.add(string);
        return array;
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
        UpdateTodo todo = new UpdateTodo();
        if(data instanceof JsonArray array){
            ArrayList<String> newList = new ArrayList<>();
            for(JsonElement element : array){
                if(element instanceof JsonPrimitive primitive)
                    newList.add(primitive.getAsString());
                else warnFailedLoadingConfig(this, element);
            }
            if(!newList.equals(strings)){
                strings.clear();
                strings.addAll(newList);
                todo.valueChanged();
            }
        }
        else warnFailedLoadingConfig(this, data);
        return todo;
    }
    @Override public boolean isModified() {
        return !strings.equals(defaultStrings);
    }
    @Override public void resetToDefault() {
        if(isModified()){
            strings.clear();
            strings.addAll(defaultStrings);
            getPage().markNeedUpdate();
            onValueChanged();
        }
    }
}
