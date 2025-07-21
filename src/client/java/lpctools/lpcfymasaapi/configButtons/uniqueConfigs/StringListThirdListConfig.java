package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@SuppressWarnings("unused")
public class StringListThirdListConfig extends UniqueStringListConfig implements IExpandableThirdList {
    boolean expanded;
    public final LPCConfigList subConfigs;
    public StringListThirdListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ImmutableList<String> defaultStrings) {
        super(parent, nameKey, defaultStrings);
        subConfigs = new LPCConfigList(parent, nameKey);
    }
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        res.add(thirdListIconPreset());
        super.getButtonOptions(res);
    }
    @Override public boolean isExpanded() {return expanded;}
    @Override public void setExpanded(boolean expanded) {
        if(this.expanded != expanded){
            this.expanded = expanded;
            getPage().updateIfCurrent();
        }
    }
    @Override public @NotNull Collection<ILPCConfig> getConfigs() {return subConfigs.getConfigs();}
    @Override public void setAlignedIndent(int indent) {subConfigs.setAlignedIndent(indent);}
    @Override public int getAlignedIndent() {return subConfigs.getAlignedIndent();}
    
    @Override public @Nullable JsonObject getAsJsonElement() {
        JsonObject object = new JsonObject();
        object.add(propertiesId, subConfigs.getAsJsonElement());
        object.add("strings", super.getAsJsonElement());
        object.addProperty(expandedKey, expanded);
        return object;
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
        if(data instanceof JsonObject object){
            UpdateTodo updateTodo = new UpdateTodo();
            updateTodo.combine(super.setValueFromJsonElementEx(object.get("strings")));
            if(object.get(expandedKey) instanceof JsonPrimitive primitive){
                boolean b = expanded;
                expanded = primitive.getAsBoolean();
                updateTodo.updatePage(b != expanded);
            }
            updateTodo.combine(subConfigs.setValueFromJsonElementEx(object.get(propertiesId)));
            return updateTodo;
        }
        return setValueFailed(data);
    }
}
