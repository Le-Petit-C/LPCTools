package lpctools.lpcfymasaapi.interfaces;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

@SuppressWarnings("unused")
public interface ILPCConfigList extends ILPCConfigBase, ILPCConfigReadable {
    @NotNull Collection<ILPCConfig> getConfigs();

    default boolean needAlign(){return true;}
    default <T extends ILPCConfig> T addConfig(T config){
        getConfigs().add(config);
        return config;
    }
    default void addConfigs(ILPCConfig... configs){
        for(ILPCConfig config : configs) addConfig(config);
    }

    default String getTitleFullTranslationKey(){return getFullTranslationKey() + ".title";}
    default String getTitleDisplayName(){return StringUtils.translate(getTitleFullTranslationKey());}
    @Override default ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
        return ILPCConfigReadable.defaultBuildConfigWrappers(wrapperList, getConfigs(), needAlign());
    }
    @Override default UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data){
        UpdateTodo todo = new UpdateTodo();
        if(data instanceof JsonObject jsonObject)
            for (ILPCConfig config : getConfigs())
                todo.combine(config.setValueFromParentJsonObjectEx(jsonObject).apply(config));
        else warnFailedLoadingConfig(this, data);
        return todo;
    }
    @Override default @Nullable JsonElement getAsJsonElement(){
        JsonObject listJson = new JsonObject();
        for(ILPCConfig config : getConfigs())
            config.addIntoParentJsonObject(listJson);
        return listJson;
    }
}
