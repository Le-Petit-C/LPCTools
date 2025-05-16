package lpctools.lpcfymasaapi.implementations;

import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.ThirdListConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

@SuppressWarnings("unused")
public interface ILPCConfigList extends ILPCConfigBase{
    @NotNull Collection<ILPCConfig> getConfigs();

    @Override default @NotNull String getFullTranslationKey(){return getPage().getModReference().modId + ".configs." + getNameKey();}

    default boolean needAlign(){return true;}
    default <T extends ILPCConfig> T addConfig(T config){
        getConfigs().add(config);
        return config;
    }
    //获取当前list中所有配置的遍历，包括三级列表中的和隐藏了的
    default Iterable<ILPCConfig> getAllConfigsIterable(){
        return new Iterable<>() {
            @Override public @NotNull Iterator<ILPCConfig> iterator() {
                return new Iterator<>() {
                    final Iterator<ILPCConfig> thisListIterator = getConfigs().iterator();
                    Iterator<ILPCConfig> subListIterator = null;
                    @Override public boolean hasNext() {
                        return thisListIterator.hasNext() || (subListIterator != null && subListIterator.hasNext());
                    }
                    @Override public ILPCConfig next() {
                        if(subListIterator != null){
                            if(subListIterator.hasNext())
                                return subListIterator.next();
                            else subListIterator = null;
                        }
                        if(thisListIterator.hasNext()){
                            ILPCConfig config = thisListIterator.next();
                            if(config instanceof ILPCConfigList list)
                                subListIterator = list.getAllConfigsIterable().iterator();
                            return config;
                        }
                        return null;
                    }
                };
            }
        };
    }

    default String getTitleFullTranslationKey(){return getPage().getModReference().modId + ".configs." + getNameKey() + ".title";}
    default String getTitleDisplayName(){return StringUtils.translate(getTitleFullTranslationKey());}
    default ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
        for(ILPCConfig config : getConfigs()){
            config.refreshName(needAlign());
            wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
            if(config instanceof ThirdListConfig list && list.getAsBoolean())
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
