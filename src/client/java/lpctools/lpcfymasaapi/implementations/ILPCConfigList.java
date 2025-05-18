package lpctools.lpcfymasaapi.implementations;

import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public interface ILPCConfigList extends ILPCConfigBase{
    @NotNull Collection<ILPCConfig> getConfigs();

    default boolean needAlign(){return true;}
    default <T extends ILPCConfig> T addConfig(T config){
        getConfigs().add(config);
        return config;
    }

    default String getTitleFullTranslationKey(){return getPage().getModReference().modId + ".configs." + getNameKey() + ".title";}
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
    default void loadConfigListFromJson(@NotNull JsonObject jsonObject){
        loadConfigListFromJson(jsonObject, getNameKey());
    }
    default void loadConfigListFromJson(@NotNull JsonObject jsonObject, String key){
        JsonObject json = JsonUtils.getNestedObject(jsonObject, key, true);
        if(json == null) return;
        for (ILPCConfig config : getConfigs())
            config.loadFromConfigListJson(json);
    }
    //使用此方法生成当前列表配置的Json并加到jsonObject中
    default void addConfigListIntoJson(@NotNull JsonObject jsonObject){
        addConfigListIntoJson(jsonObject, getNameKey());
    }
    default void addConfigListIntoJson(@NotNull JsonObject jsonObject, String key){
        JsonObject listJson = new JsonObject();
        for(ILPCConfig config : getConfigs())
            config.addIntoConfigListJson(listJson);
        jsonObject.add(key, listJson);
    }
}
