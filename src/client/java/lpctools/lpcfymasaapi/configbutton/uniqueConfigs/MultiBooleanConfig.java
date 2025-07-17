package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.IConfigBoolean;
import fi.dy.masa.malilib.config.IConfigResettable;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MultiBooleanConfig extends ThirdListConfig implements IConfigResettable {
    public final @NotNull ImmutableList<Boolean> defaultBooleans;
    public final @NotNull ImmutableList<UniqueBooleanConfig> booleans;
    public MultiBooleanConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull ImmutableList<Boolean> defaultBooleans, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        this.defaultBooleans = defaultBooleans;
        ArrayList<UniqueBooleanConfig> booleans = new ArrayList<>();
        for(int a = 0; a < defaultBooleans.size(); ++a){
            UniqueBooleanConfig config = new UniqueBooleanConfig(this, "boolean" + a, defaultBooleans.get(a), null){
                @Override public void onValueChanged() {super.onValueChanged();MultiBooleanConfig.this.onValueChanged();}};
            booleans.add(config); super.addConfig(config);
        }
        this.booleans = ImmutableList.copyOf(booleans);
    }
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
        super.getButtonOptions(res);
        if(!isExpanded())
            for(UniqueBooleanConfig configBoolean : booleans){
                res.add(
                    new ButtonOption(1, (button, mouseButton) -> configBoolean.toggleBooleanValue(),
                        ()->configBoolean.getTitleTranslation() + ':' + (configBoolean.getBooleanValue() ? "§2true" : "§4false"), buttonGenericAllocator)
                );
            }
    }
    public static final String booleansKey = "booleans";
    @Override public @NotNull JsonObject getAsJsonElement() {
        JsonObject res = super.getAsJsonElement();
        JsonArray array = new JsonArray();
        for(IConfigBoolean configBoolean : booleans)
            array.add(configBoolean.getBooleanValue());
        res.add(booleansKey, array);
        return res;
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        super.setValueFromJsonElementEx(element);
        UpdateTodo todo = new UpdateTodo();
        if(element instanceof JsonObject object && object.get(booleansKey) instanceof JsonArray array){
            try{
                for(int a = 0; a < booleans.size(); ++a)
                    if(array.get(a) instanceof JsonElement element1){
                        booleans.get(a).setValueFromJsonElement(element1);
                    }
            } catch (IndexOutOfBoundsException ignored){}
        }
        return todo;
    }
    @Override public boolean isModified() {
        for(int a = 0; a < defaultBooleans.size(); ++a){
            if(!defaultBooleans.get(a).equals(booleans.get(a).getBooleanValue()))
                return true;
        }
        return false;
    }
    @Override public void resetToDefault() {
        for(int a = 0; a < booleans.size(); ++a)
            booleans.get(a).setBooleanValue(defaultBooleans.get(a));
    }
}
