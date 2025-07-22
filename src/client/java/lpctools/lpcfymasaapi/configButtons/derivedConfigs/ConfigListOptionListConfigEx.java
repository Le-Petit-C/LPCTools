package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

public class ConfigListOptionListConfigEx<T> extends ArrayOptionListConfig<@NotNull ImmutablePair<ILPCConfigList, T>> implements IThirdListBase {
    public ConfigListOptionListConfigEx(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {
        this(parent, nameKey, null);
    }
    public ConfigListOptionListConfigEx(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
    }
    @Override public @NotNull Collection<ILPCConfig> getConfigs() {return getCurrentUserdata().left.getConfigs();}
    @Override public void setAlignedIndent(int indent) {getCurrentUserdata().left.setAlignedIndent(indent);}
    @Override public int getAlignedIndent() {return getCurrentUserdata().left.getAlignedIndent();}
    
    public static final String superJsonId = "selected";
    public static final String selectionsId = "selections";
    @Override public @NotNull JsonElement getAsJsonElement() {
        JsonObject baseObject = new JsonObject();
        baseObject.add(superJsonId, super.getAsJsonElement());
        JsonObject listObjects = new JsonObject();
        getCurrentOptionData().options().forEach(list->
            listObjects.add(list.translationKey(), list.userData().left.getAsJsonElement()));
        baseObject.add(selectionsId, listObjects);
        return baseObject;
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        UpdateTodo todo = new UpdateTodo();
        if(element instanceof JsonObject object
            && object.get(superJsonId) instanceof JsonElement superJson
            && object.get(selectionsId) instanceof JsonObject selections){
            todo.combine(super.setValueFromJsonElementEx(superJson));
            for(OptionData<ImmutablePair<ILPCConfigList, T>> list : getCurrentOptionData().options()){
                ILPCConfigList list1 = list.userData().left;
                todo.combine(list1.setValueFromJsonElementEx(selections.get(list.getStringValue())));
            }
        }
        else warnFailedLoadingConfig(this, element);
        return todo;
    }
    @Override public void onValueChanged() {
        super.onValueChanged();
        getPage().markNeedUpdate();
    }
    public ILPCConfigList addList(String nameKey, T userData){
        return addOption(nameKey, new ImmutablePair<>(new LPCConfigList(getParent(), getNameKey() + '.' + nameKey), userData)).left;
    }
}
