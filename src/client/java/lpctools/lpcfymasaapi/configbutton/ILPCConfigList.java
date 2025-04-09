package lpctools.lpcfymasaapi.configbutton;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.RangeLimitConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

@SuppressWarnings("unused")
public interface ILPCConfigList {
    <T extends ILPCConfig> T addConfig(T config);
    String getNameKey();
    @NotNull LPCConfigPage getPage();
    @NotNull Iterable<ILPCConfig> getConfigs();
    default boolean needAlign(){return true;}

    default String getTitleFullTranslationKey(){return getPage().getModReference().modId + ".configs." + getNameKey() + ".title";}
    default String getTitleDisplayName(){return StringUtils.translate(getTitleFullTranslationKey());}
    default String getFullTranslationKey(){return getPage().getModReference().modId + ".configs." + getNameKey();}
    //加入的配置无法删除
    //加入的配置不会立刻从文件中加载已有的设定，直到loadFromConfigPageJson被调用（一般来自LPCConfigPage中的load()），
    // 一般情况下是malilib初始化时或者加入了一个世界时malilib会调用load()，此时会从文件中加载所有保存的配置
    default BooleanConfig addBooleanConfig(@NotNull String nameKey, boolean defaultBoolean){
        return addConfig(new BooleanConfig(this, nameKey, defaultBoolean));
    }
    default BooleanConfig addBooleanConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable IValueRefreshCallback callback){
        return addConfig(new BooleanConfig(this, nameKey, defaultBoolean, callback));
    }
    default IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger){
        return addConfig(new IntegerConfig(this, nameKey, defaultInteger));
    }
    default IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, @Nullable IValueRefreshCallback callback){
        return addConfig(new IntegerConfig(this, nameKey, defaultInteger, callback));
    }
    default IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, int minValue, int maxValue){
        return addConfig(new IntegerConfig(this, nameKey, defaultInteger, minValue, maxValue));
    }
    default IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, int minValue, int maxValue, @Nullable IValueRefreshCallback callback){
        return addConfig(new IntegerConfig(this, nameKey, defaultInteger, minValue, maxValue, callback));
    }
    default DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble){
        return addConfig(new DoubleConfig(this, nameKey, defaultDouble));
    }
    default DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, @Nullable IValueRefreshCallback callback){
        return addConfig(new DoubleConfig(this, nameKey, defaultDouble, callback));
    }
    default DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, double minValue, double maxValue){
        return addConfig(new DoubleConfig(this, nameKey, defaultDouble, minValue, maxValue));
    }
    default DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, double minValue, double maxValue, @Nullable IValueRefreshCallback callback){
        return addConfig(new DoubleConfig(this, nameKey, defaultDouble, minValue, maxValue, callback));
    }
    default HotkeyConfig addHotkeyConfig(@NotNull String nameKey, @Nullable String defaultStorageString, @NotNull IHotkeyCallback callBack){
        return addConfig(new HotkeyConfig(this, nameKey, defaultStorageString, callBack));
    }
    default BooleanHotkeyConfig addBooleanHotkeyConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        return addConfig(new BooleanHotkeyConfig(this, nameKey, defaultBoolean, defaultStorageString));
    }
    default BooleanHotkeyConfig addBooleanHotkeyConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable IValueRefreshCallback callback){
        return addConfig(new BooleanHotkeyConfig(this, nameKey, defaultBoolean, defaultStorageString, callback));
    }
    default StringListConfig addStringListConfig(@NotNull String nameKey, @Nullable ImmutableList<String> defaultValue){
        return addConfig(new StringListConfig(this, nameKey, defaultValue));
    }
    default StringListConfig addStringListConfig(@NotNull String nameKey, @Nullable ImmutableList<String> defaultValue, @Nullable IValueRefreshCallback callback){
        return addConfig(new StringListConfig(this, nameKey, defaultValue, callback));
    }
    default ConfigOpenGuiConfig addConfigOpenGuiConfig(@Nullable String defaultStorageString){
        return addConfig(new ConfigOpenGuiConfig(this, defaultStorageString));
    }
    default ThirdListConfig addThirdListConfig(@NotNull String nameKey, boolean defaultBoolean){
        return addConfig(new ThirdListConfig(this, nameKey, defaultBoolean));
    }
    default <T> OptionListConfig<T> addOptionListConfig(@NotNull String nameKey){
        return addConfig(new OptionListConfig<>(this, nameKey));
    }
    default <T> OptionListConfig<T> addOptionListConfig(@NotNull String nameKey, @Nullable IValueRefreshCallback callback){
        return addConfig(new OptionListConfig<>(this, nameKey, callback));
    }
    default <T extends IntSupplier & IntConsumer> IntegerListConfig<T> addIntegerListConfig(@NotNull String nameKey){
        return addConfig(new IntegerListConfig<>(this, nameKey));
    }
    default <T extends IntSupplier & IntConsumer> IntegerListConfig<T> addIntegerListConfig(@NotNull String nameKey, @Nullable IValueRefreshCallback callback){
        return addConfig(new IntegerListConfig<>(this, nameKey, callback));
    }
    default StringConfig addStringConfig(@NotNull String nameKey, @Nullable String defaultString, @Nullable IValueRefreshCallback callback){
        return addConfig(new StringConfig(this, nameKey, defaultString, callback));
    }
    default StringConfig addStringConfig(@NotNull String nameKey, @Nullable String defaultString){
        return addConfig(new StringConfig(this, nameKey, defaultString));
    }
    default StringConfig addStringConfig(@NotNull String nameKey,@Nullable IValueRefreshCallback callback){
        return addConfig(new StringConfig(this, nameKey, callback));
    }
    default StringConfig addStringConfig(@NotNull String nameKey){
        return addConfig(new StringConfig(this, nameKey));
    }
    default RangeLimitConfig addRangeLimitConfig(boolean defaultBoolean, String defaultPrefix){
        return addConfig(new RangeLimitConfig(this, defaultBoolean, defaultPrefix));
    }
    //列表中配置项的配置值可能发生了更新，调用此方法让配置做出反应
    default void callRefresh(){
        for(ILPCConfig config : getConfigs())
            config.callRefresh();
    }
    default ArrayList<GuiConfigsBase.ConfigOptionWrapper>
    buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
        for(ILPCConfig config : getConfigs()){
            config.refreshName(needAlign());
            wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config.IGetConfig()));
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
