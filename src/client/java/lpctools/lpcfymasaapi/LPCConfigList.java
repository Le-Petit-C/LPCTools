package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.configbutton.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

//配置列表
public class LPCConfigList {
    public LPCConfigPage getPage(){return parent;}
    public LPCConfigList(LPCConfigPage parent, String translationKey){
        this.parent = parent;
        this.translationKey = translationKey;
    }
    //加入的配置无法删除，但是你可以将LPCConfig.enabled置为false让它不显示
    //加入的配置不会立刻从文件中加载已有的设定，直到loadFromConfigPageJson被调用（一般来自LPCConfigPage中的load()），
    // 一般情况下是malilib初始化时或者加入了一个世界时malilib会调用load()，此时会从文件中加载所有保存的配置
    public void addConfig(ILPCConfig config){
        configs.add(config);
        if(config.hasHotkey())
            hasHotkeyConfig = true;
    }
    public BooleanConfig addBooleanConfig(String nameKey, boolean defaultBoolean){
        BooleanConfig config = new BooleanConfig(this, nameKey, defaultBoolean);
        addConfig(config);
        return config;
    }
    public BooleanConfig addBooleanConfig(String nameKey, boolean defaultBoolean, IValueRefreshCallback callback){
        BooleanConfig config = new BooleanConfig(this, nameKey, defaultBoolean, callback);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger){
        IntegerConfig config = new IntegerConfig(this, nameKey, defaultInteger);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger, IValueRefreshCallback callback){
        IntegerConfig config = new IntegerConfig(this, nameKey, defaultInteger, callback);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger, int minValue, int maxValue){
        IntegerConfig config = new IntegerConfig(this, nameKey, defaultInteger, minValue, maxValue);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger, int minValue, int maxValue, IValueRefreshCallback callback){
        IntegerConfig config = new IntegerConfig(this, nameKey, defaultInteger, minValue, maxValue, callback);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble){
        DoubleConfig config = new DoubleConfig(this, nameKey, defaultDouble);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble, IValueRefreshCallback callback){
        DoubleConfig config = new DoubleConfig(this, nameKey, defaultDouble, callback);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble, double minValue, double maxValue){
        DoubleConfig config = new DoubleConfig(this, nameKey, defaultDouble, minValue, maxValue);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble, double minValue, double maxValue, IValueRefreshCallback callback){
        DoubleConfig config = new DoubleConfig(this, nameKey, defaultDouble, minValue, maxValue, callback);
        addConfig(config);
        return config;
    }
    public HotkeyConfig addHotkeyConfig(String nameKey, String defaultStorageString, IHotkeyCallback callBack){
        HotkeyConfig config = new HotkeyConfig(this, nameKey, defaultStorageString, callBack);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String nameKey, boolean defaultBoolean, String defaultStorageString){
        BooleanHotkeyConfig config = new BooleanHotkeyConfig(this, nameKey, defaultBoolean, defaultStorageString);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String nameKey, boolean defaultBoolean, String defaultStorageString, IValueRefreshCallback callback){
        BooleanHotkeyConfig config = new BooleanHotkeyConfig(this, nameKey, defaultBoolean, defaultStorageString, callback);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String nameKey, ImmutableList<String> defaultValue){
        StringListConfig config = new StringListConfig(this, nameKey, defaultValue);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String nameKey, ImmutableList<String> defaultValue, IValueRefreshCallback callback){
        StringListConfig config = new StringListConfig(this, nameKey, defaultValue, callback);
        addConfig(config);
        return config;
    }
    public ConfigOpenGuiConfig addConfigOpenGuiConfig(String defaultStorageString){
        ConfigOpenGuiConfig config = new ConfigOpenGuiConfig(this, defaultStorageString);
        addConfig(config);
        return config;
    }
    public ThirdListConfig addThirdListConfig(String nameKey, boolean defaultBoolean){
        ThirdListConfig config = new ThirdListConfig(this, nameKey, defaultBoolean, null);
        addConfig(config);
        return config;
    }
    public ThirdListConfig addThirdListConfig(String nameKey, boolean defaultBoolean, ThirdListConfig parent){
        ThirdListConfig config = new ThirdListConfig(this, nameKey, defaultBoolean, parent);
        addConfig(config);
        return config;
    }
    public <T> OptionListConfig<T> addOptionListConfig(String nameKey){
        OptionListConfig<T> config = new OptionListConfig<>(this, nameKey);
        addConfig(config);
        return config;
    }
    public <T> OptionListConfig<T> addOptionListConfig(String nameKey, IValueRefreshCallback callback){
        OptionListConfig<T> config = new OptionListConfig<>(this, nameKey, callback);
        addConfig(config);
        return config;
    }
    public <T extends IntSupplier & IntConsumer> IntegerListConfig<T> addIntegerListConfig(String nameKey){
        IntegerListConfig<T> config = new IntegerListConfig<>(this, nameKey);
        addConfig(config);
        return config;
    }
    public <T extends IntSupplier & IntConsumer> IntegerListConfig<T> addIntegerListConfig(String nameKey, IValueRefreshCallback callback){
        IntegerListConfig<T> config = new IntegerListConfig<>(this, nameKey, callback);
        addConfig(config);
        return config;
    }
    public String getTitleFullTranslationKey(){return parent.getModReference().modId + ".configs." + translationKey + ".title";}
    public String getTranslationKey(){return translationKey;}
    public String getFullTranslationKey(){return getPage().getModReference().modId + ".configs." + getTranslationKey();}
    public String getTitleDisplayName(){return StringUtils.translate(getTitleFullTranslationKey());}
    public boolean hasHotkeyConfig() {return hasHotkeyConfig;}

    @NotNull final ArrayList<ILPCConfig> configs = new ArrayList<>();
    //使用此方法从PageJson中加载列表配置
    void loadFromConfigPageJson(@NotNull JsonObject configPageJson){
        JsonObject json = JsonUtils.getNestedObject(configPageJson, getTranslationKey(), true);
        if(json == null) return;
        for (ILPCConfig config : configs)
            config.loadFromConfigListJson(json);
    }
    //使用此方法生成当前列表配置的Json并加PageJson中
    void addIntoConfigPageJson(@NotNull JsonObject pageJson){
        JsonObject listJson = new JsonObject();
        for(ILPCConfig config : configs)
            config.addIntoConfigListJson(listJson);
        pageJson.add(getTranslationKey(), listJson);
    }
    //列表中的数据可能发生了更新，调用此方法让配置做出反应
    void callRefresh(){
        for(ILPCConfig config : configs)
            config.callRefresh();
    }

    private final String translationKey;
    private final LPCConfigPage parent;
    private boolean hasHotkeyConfig;
}
