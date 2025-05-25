package lpctools.lpcfymasaapi.implementations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

@SuppressWarnings("unused")
public interface ILPCConfigList extends ILPCConfigBase{
    @NotNull Collection<ILPCConfig> getConfigs();

    default boolean needAlign(){return true;}
    default <T extends ILPCConfig> T addConfig(T config){
        getConfigs().add(config);
        return config;
    }

    default String getTitleFullTranslationKey(){return getFullTranslationKey() + ".title";}
    default String getTitleDisplayName(){return StringUtils.translate(getTitleFullTranslationKey());}
    default ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
        for(ILPCConfig config : getConfigs()){
            config.refreshName(needAlign());
            wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
            if(config instanceof ILPCConfigList list)
                list.buildConfigWrappers(wrapperList);
        }
        return wrapperList;
    }
    //使用此方法从jsonObject中加载列表配置
    @Override default void setValueFromJsonElement(@NotNull JsonElement data){
        if(data instanceof JsonObject jsonObject)
            for (ILPCConfig config : getConfigs())
                config.setValueFromParentJsonObject(jsonObject);
        else warnFailedLoadingConfig(this, data);
    }
    //使用此方法生成当前列表配置的Json并加到jsonObject中
    @Override default @Nullable JsonElement getAsJsonElement(){
        JsonObject listJson = new JsonObject();
        for(ILPCConfig config : getConfigs())
            config.addIntoParentJsonObject(listJson);
        return listJson;
    }
}
