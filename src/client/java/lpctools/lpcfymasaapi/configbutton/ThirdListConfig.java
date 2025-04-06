package lpctools.lpcfymasaapi.configbutton;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.util.GuiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

//第三级列表，配置中切换true或false可以展开或收起内含的配置项
@SuppressWarnings("unused")
public class ThirdListConfig extends BooleanConfig{
    @NotNull public final ArrayList<ILPCConfig> thirdList = new ArrayList<>();
    @Nullable public final ThirdListConfig parent;
    public ThirdListConfig(LPCConfigList list, String nameKey, boolean defaultBoolean, @Nullable ThirdListConfig parent) {
        super(list, nameKey, defaultBoolean);
        this.parent = parent;
        setEnabled(parent == null || (parent.isEnabled() && parent.getAsBoolean()));
        lastValue = isEnabled() && defaultBoolean;
        setCallback(new ThirdListCallback(this));
    }
    public BooleanConfig addBooleanConfig(String nameKey, boolean defaultBoolean){
        BooleanConfig config = super.getList().addBooleanConfig(nameKey, defaultBoolean);
        addConfig(config);
        return config;
    }
    public BooleanConfig addBooleanConfig(String nameKey, boolean defaultBoolean, IValueRefreshCallback callback){
        BooleanConfig config = super.getList().addBooleanConfig(nameKey, defaultBoolean, callback);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger){
        IntegerConfig config = super.getList().addIntegerConfig(nameKey, defaultInteger);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger, IValueRefreshCallback callback){
        IntegerConfig config = super.getList().addIntegerConfig(nameKey, defaultInteger, callback);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger, int minValue, int maxValue){
        IntegerConfig config = super.getList().addIntegerConfig(nameKey, defaultInteger, minValue, maxValue);
        addConfig(config);
        return config;
    }
    public IntegerConfig addIntegerConfig(String nameKey, int defaultInteger, int minValue, int maxValue, IValueRefreshCallback callback){
        IntegerConfig config = super.getList().addIntegerConfig(nameKey, defaultInteger, minValue, maxValue, callback);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble){
        DoubleConfig config = super.getList().addDoubleConfig(nameKey, defaultDouble);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble, IValueRefreshCallback callback){
        DoubleConfig config = super.getList().addDoubleConfig(nameKey, defaultDouble, callback);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble, double minValue, double maxValue){
        DoubleConfig config = super.getList().addDoubleConfig(nameKey, defaultDouble, minValue, maxValue);
        addConfig(config);
        return config;
    }
    public DoubleConfig addDoubleConfig(String nameKey, double defaultDouble, double minValue, double maxValue, IValueRefreshCallback callback){
        DoubleConfig config = super.getList().addDoubleConfig(nameKey, defaultDouble, minValue, maxValue, callback);
        addConfig(config);
        return config;
    }
    public HotkeyConfig addHotkeyConfig(String nameKey, String defaultStorageString, IHotkeyCallback callBack){
        HotkeyConfig config = super.getList().addHotkeyConfig(nameKey, defaultStorageString, callBack);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String nameKey, boolean defaultBoolean, String defaultStorageString){
        BooleanHotkeyConfig config = super.getList().addBooleanHotkeyConfig(nameKey, defaultBoolean, defaultStorageString);
        addConfig(config);
        return config;
    }
    public BooleanHotkeyConfig addBooleanHotkeyConfig(String nameKey, boolean defaultBoolean, String defaultStorageString, IValueRefreshCallback callback){
        BooleanHotkeyConfig config = super.getList().addBooleanHotkeyConfig(nameKey, defaultBoolean, defaultStorageString, callback);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String nameKey, @Nullable ImmutableList<String> defaultValue){
        StringListConfig config = super.getList().addStringListConfig(nameKey, defaultValue);
        addConfig(config);
        return config;
    }
    public StringListConfig addStringListConfig(String nameKey, ImmutableList<String> defaultValue, IValueRefreshCallback callback){
        StringListConfig config = super.getList().addStringListConfig(nameKey, defaultValue, callback);
        addConfig(config);
        return config;
    }
    public ConfigOpenGuiConfig addConfigOpenGuiConfig(String defaultStorageString){
        ConfigOpenGuiConfig config = super.getList().addConfigOpenGuiConfig(defaultStorageString);
        addConfig(config);
        return config;
    }
    public ThirdListConfig addThirdListConfig(String nameKey, boolean defaultBoolean){
        ThirdListConfig config = super.getList().addThirdListConfig(nameKey, defaultBoolean, this);
        addConfig(config);
        return config;
    }
    public <T> OptionListConfig<T> addOptionListConfig(String nameKey){
        OptionListConfig<T> config = super.getList().addOptionListConfig(nameKey);
        addConfig(config);
        return config;
    }
    public <T> OptionListConfig<T> addOptionListConfig(String nameKey, IValueRefreshCallback callback){
        OptionListConfig<T> config = super.getList().addOptionListConfig(nameKey, callback);
        addConfig(config);
        return config;
    }
    public <T extends IntSupplier & IntConsumer> IntegerListConfig<T> addIntegerListConfig(String nameKey){
        IntegerListConfig<T> config = super.getList().addIntegerListConfig(nameKey);
        addConfig(config);
        return config;
    }
    public <T extends IntSupplier & IntConsumer> IntegerListConfig<T> addIntegerListConfig(String nameKey, IValueRefreshCallback callback){
        IntegerListConfig<T> config = super.getList().addIntegerListConfig(nameKey, callback);
        addConfig(config);
        return config;
    }

    private boolean lastValue;
    private void refreshSingle(ILPCConfig config){
        boolean parentEnable = isEnabled() && getAsBoolean();
        if(config instanceof ThirdListConfig thCon)
            thCon.refreshEnable();
        else config.setEnabled(parentEnable);
    }
    private boolean refreshEnable(){
        if(parent != null) setEnabled(parent.isEnabled() && parent.getAsBoolean());
        boolean currentValue = isEnabled() && getAsBoolean();
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
        config.setEnabled(isEnabled() && getAsBoolean());
    }
    private record ThirdListCallback(ThirdListConfig parent) implements IValueRefreshCallback {
        @Override public void valueRefreshCallback() {
            if (parent.refreshEnable() && GuiUtils.isInTextOrGui())
                parent.getList().getPage().showPage();
        }
    }
}
