package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.lpcfymasaapi.implementations.ILPCConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.implementations.IThirdListBase;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

public class ConfigListOptionListConfigEx<T extends ILPCConfigList> extends ArrayOptionListConfig<T> implements IThirdListBase {
    public ConfigListOptionListConfigEx(@NotNull ILPCConfigList parent, @NotNull String nameKey) {
        this(parent, nameKey, null);
    }
    public ConfigListOptionListConfigEx(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
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
    @Override public void setValueFromJsonElement(@NotNull JsonElement element) {
        if(element instanceof JsonObject object
            && object.get(superJsonId) instanceof JsonElement superJson
            && object.get(selectionsId) instanceof JsonObject selections){
            super.setValueFromJsonElement(superJson);
            for(OptionData<T> list : getCurrentOptionData().options()){
                T data = list.userData();
                if (data == null) continue;
                data.setValueFromParentJsonObject(selections);
            }
        }
        else warnFailedLoadingConfig(this, element);
    }
    @Override public void onValueChanged() {
        super.onValueChanged();
        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc.currentScreen != null && getPage().get() == mc.currentScreen)
            getPage().showPage();
    }
    public T addList(T list){
        addOption(list.getTitleFullTranslationKey(), list);
        return list;
    }
    @Override public String getAlignSpaces() {return getParentSpaces();}
}
