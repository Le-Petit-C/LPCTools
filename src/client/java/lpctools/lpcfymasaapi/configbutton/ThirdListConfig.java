package lpctools.lpcfymasaapi.configbutton;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.util.GuiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

//第三级列表，配置中切换true或false可以展开或收起内含的配置项
public class ThirdListConfig extends BooleanConfig{
    @NotNull public final ArrayList<ILPCConfig> thirdList = new ArrayList<>();
    @Nullable public final ThirdListConfig parent;
    public ThirdListConfig(LPCConfigList list, String name, boolean defaultBoolean, @Nullable ThirdListConfig parent) {
        super(list, name, defaultBoolean);
        this.parent = parent;
        if(parent != null) enabled = parent.enabled && parent.getAsBoolean();
        else enabled = true;
        lastValue = enabled && defaultBoolean;
        setCallback(new ThirdListCallback(this));
    }
    public BooleanConfig addBooleanConfig(String name, boolean defaultBoolean){
        BooleanConfig config = super.list.addBooleanConfig(name, defaultBoolean);
        addConfig(config);
        return config;
    }
    public BooleanConfig addBooleanConfig(String name, boolean defaultBoolean, IValueRefreshCallback callback){
        BooleanConfig config = super.list.addBooleanConfig(name, defaultBoolean, callback);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String name, int defaultInteger){
        IntegerConfig config = super.list.addIntegerConfig(name, defaultInteger);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String name, int defaultInteger, IValueRefreshCallback callback){
        IntegerConfig config = super.list.addIntegerConfig(name, defaultInteger, callback);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String name, int defaultInteger, int minValue, int maxValue){
        IntegerConfig config = super.list.addIntegerConfig(name, defaultInteger, minValue, maxValue);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String name, int defaultInteger, int minValue, int maxValue, IValueRefreshCallback callback){
        IntegerConfig config = super.list.addIntegerConfig(name, defaultInteger, minValue, maxValue, callback);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String name, double defaultDouble){
        DoubleConfig config = super.list.addDoubleConfig(name, defaultDouble);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String name, double defaultDouble, IValueRefreshCallback callback){
        DoubleConfig config = super.list.addDoubleConfig(name, defaultDouble, callback);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String name, double defaultDouble, double minValue, double maxValue){
        DoubleConfig config = super.list.addDoubleConfig(name, defaultDouble, minValue, maxValue);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String name, double defaultDouble, double minValue, double maxValue, IValueRefreshCallback callback){
        DoubleConfig config = super.list.addDoubleConfig(name, defaultDouble, minValue, maxValue, callback);
        addConfig(config);
        return config;
    }
    public HotkeyConfig addHotkeyConfig(String name, String defaultStorageString, IHotkeyCallback callBack){
        HotkeyConfig config = super.list.addHotkeyConfig(name, defaultStorageString, callBack);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String name, boolean defaultBoolean, String defaultStorageString){
        BooleanHotkeyConfig config = super.list.addBooleanHotkeyConfig(name, defaultBoolean, defaultStorageString);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String name, boolean defaultBoolean, String defaultStorageString, IValueRefreshCallback callback){
        BooleanHotkeyConfig config = super.list.addBooleanHotkeyConfig(name, defaultBoolean, defaultStorageString, callback);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String name, ImmutableList<String> defaultValue){
        StringListConfig config = super.list.addStringListConfig(name, defaultValue);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String name, ImmutableList<String> defaultValue, IValueRefreshCallback callback){
        StringListConfig config = super.list.addStringListConfig(name, defaultValue, callback);
        addConfig(config);
        return config;
    }
    public ConfigOpenGuiConfig addConfigOpenGuiConfig(String defaultStorageString){
        ConfigOpenGuiConfig config = super.list.addConfigOpenGuiConfig(defaultStorageString);
        addConfig(config);
        return config;
    }
    public ThirdListConfig addThirdListConfig(String name, boolean defaultBoolean){
        ThirdListConfig config = super.list.addThirdListConfig(name, defaultBoolean, this);
        addConfig(config);
        return config;
    }
    public <T> OptionListConfig<T> addOptionListConfig(String name){
        OptionListConfig<T> config = super.list.addOptionListConfig(name);
        addConfig(config);
        return config;
    }
    public <T> OptionListConfig<T> addOptionListConfig(String name, IValueRefreshCallback callback){
        OptionListConfig<T> config = super.list.addOptionListConfig(name, callback);
        addConfig(config);
        return config;
    }

    private boolean lastValue;
    private void refreshSingle(ILPCConfig config){
        boolean parentEnable = enabled && getAsBoolean();
        if(config instanceof ThirdListConfig thCon)
            thCon.refreshEnable();
        else config.setEnabled(parentEnable);
    }
    private boolean refreshEnable(){
        if(parent != null) enabled = parent.enabled && parent.getAsBoolean();
        boolean currentValue = enabled && getAsBoolean();
        if(currentValue != lastValue){
            for(ILPCConfig config : thirdList)
                refreshSingle(config);
            lastValue = currentValue;
            return true;
        }
        else return false;
    }
    private void addConfig(ILPCConfig config){
        thirdList.add(config);
        config.setEnabled(enabled && getAsBoolean());
    }
    private record ThirdListCallback(ThirdListConfig parent) implements IValueRefreshCallback {
        @Override public void valueRefreshCallback() {
            if (parent.refreshEnable() && GuiUtils.isInTextOrGui())
                parent.list.getPage().showPage();
        }
    }
}
