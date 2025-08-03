package lpctools.lpcfymasaapi.interfaces;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

@SuppressWarnings("unused")
public interface ILPCConfigList extends ILPCConfigBase, ILPCConfigReadable{
    @Override @NotNull Collection<ILPCConfig> getConfigs();

    default boolean needAlign(){return true;}
    default <T extends ILPCConfig> T addConfig(T config){
        getConfigs().add(config);
        return config;
    }
    default void addConfigs(ILPCConfig... configs){
        for(ILPCConfig config : configs) addConfig(config);
    }

    default String getTitleFullTranslationKey(){return getFullTranslationKey() + ".title";}
    default String getTitleDisplayName(){return Text.translatable(getTitleFullTranslationKey()).getString();}
    @Override default UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data){
        UpdateTodo todo = new UpdateTodo();
        if(data instanceof JsonObject jsonObject)
            for (ILPCConfig config : getConfigs())
                todo.combine(config.setValueFromParentJsonObjectEx(jsonObject).apply(config));
        else warnFailedLoadingConfig(this, data);
        return todo;
    }
    @Override default @Nullable JsonObject getAsJsonElement(){
        JsonObject listJson = new JsonObject();
        for(ILPCConfig config : getConfigs())
            config.addIntoParentJsonObject(listJson);
        return listJson;
    }
    @Override default void close() throws Exception {
        ILPCConfigReadable.super.close();
        getConfigs().clear();
    }
}
