package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

public class ConfigListOptionListConfigEx<T extends ILPCConfigList> extends ArrayOptionListConfig<T> implements IThirdListBase {
    public ConfigListOptionListConfigEx(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {
        this(parent, nameKey, null);
    }
    public ConfigListOptionListConfigEx(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
    }
    @Override public @NotNull Collection<ILPCConfig> getConfigs() {
        return getCurrentUserdata().getConfigs();
    }
    public static final String superJsonId = "selected";
    public static final String selectionsId = "selections";
    @Override public @NotNull JsonElement getAsJsonElement() {
        JsonObject baseObject = new JsonObject();
        baseObject.add(superJsonId, super.getAsJsonElement());
        JsonObject listObjects = new JsonObject();
        for(OptionData<T> list : getCurrentOptionData().options()){
            T data = list.userData();
            if (data == null) continue;
            data.addIntoParentJsonObject(listObjects);
        }
        baseObject.add(selectionsId, listObjects);
        return baseObject;
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        UpdateTodo todo = new UpdateTodo();
        if(element instanceof JsonObject object
            && object.get(superJsonId) instanceof JsonElement superJson
            && object.get(selectionsId) instanceof JsonObject selections){
            todo.combine(super.setValueFromJsonElementEx(superJson));
            for(OptionData<T> list : getCurrentOptionData().options()){
                T data = list.userData();
                if (data == null) continue;
                data.setValueFromParentJsonObject(selections);
            }
        }
        else warnFailedLoadingConfig(this, element);
        return todo;
    }
    @Override public void onValueChanged() {
        super.onValueChanged();
        getPage().updateIfCurrent();
    }
    public <U extends T> U addList(U list){
        addOption(list.getTitleFullTranslationKey(), list);
        return list;
    }
    @Override public String getAlignSpaces() {return getParentSpaces();}
}
