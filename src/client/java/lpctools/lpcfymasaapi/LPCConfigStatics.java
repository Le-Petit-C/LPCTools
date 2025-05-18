package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configbutton.derivedConfigs.*;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.*;
import lpctools.lpcfymasaapi.implementations.ILPCConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class LPCConfigStatics {
    //加入的配置无法删除
    //加入的配置不会立刻从文件中加载已有的设定，直到loadFromConfigPageJson被调用（一般来自LPCConfigPage中的load()），
    //一般情况下是malilib初始化时或者加入了一个世界时malilib会调用load()，此时会从文件中加载所有保存的配置
    public static BooleanConfig addBooleanConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean){
        return list.addConfig(new BooleanConfig(list, nameKey, defaultBoolean));
    }
    public static BooleanConfig addBooleanConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new BooleanConfig(list, nameKey, defaultBoolean, callback));
    }
    public static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger));
    }
    public static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger, callback));
    }
    public static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger, int minValue, int maxValue){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger, minValue, maxValue));
    }
    public static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger, int minValue, int maxValue, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger, minValue, maxValue, callback));
    }
    public static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble));
    }
    public static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble, callback));
    }
    public static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble, double minValue, double maxValue){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble, minValue, maxValue));
    }
    public static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble, double minValue, double maxValue, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble, minValue, maxValue, callback));
    }
    public static HotkeyConfig addHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultStorageString, @NotNull IHotkeyCallback callBack){
        return list.addConfig(new HotkeyConfig(list, nameKey, defaultStorageString, callBack));
    }
    public static BooleanHotkeyConfig addBooleanHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        return list.addConfig(new BooleanHotkeyConfig(list, nameKey, defaultBoolean, defaultStorageString));
    }
    public static BooleanHotkeyConfig addBooleanHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new BooleanHotkeyConfig(list, nameKey, defaultBoolean, defaultStorageString, callback));
    }
    public static StringListConfig addStringListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ImmutableList<String> defaultValue){
        return list.addConfig(new StringListConfig(list, nameKey, defaultValue));
    }
    public static StringListConfig addStringListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ImmutableList<String> defaultValue, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new StringListConfig(list, nameKey, defaultValue, callback));
    }
    public static ConfigOpenGuiConfig addConfigOpenGuiConfig(ILPCConfigList list, @Nullable String defaultStorageString){
        return list.addConfig(new ConfigOpenGuiConfig(list, defaultStorageString));
    }
    public static ThirdListConfig addThirdListConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean){
        return list.addConfig(new ThirdListConfig(list, nameKey, defaultBoolean));
    }
    public static <T> OptionListConfig addOptionListConfig(ILPCConfigList list, @NotNull String nameKey, IConfigOptionListEntry defaultOption){
        return list.addConfig(new OptionListConfig(list, nameKey, defaultOption));
    }
    public static <T> OptionListConfig addOptionListConfig(ILPCConfigList list, @NotNull String nameKey, IConfigOptionListEntry defaultOption, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new OptionListConfig(list, nameKey, defaultOption, callback));
    }
    public static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(ILPCConfigList list, @NotNull String nameKey, Iterable<T> values){
        return list.addConfig(new IntegerArrayListConfig<>(list, nameKey, values));
    }
    public static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(ILPCConfigList list, @NotNull String nameKey, Iterable<T> values, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new IntegerArrayListConfig<>(list, nameKey, values));
    }
    public static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultString, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new StringConfig(list, nameKey, defaultString, callback));
    }
    public static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultString){
        return list.addConfig(new StringConfig(list, nameKey, defaultString));
    }
    public static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new StringConfig(list, nameKey, callback));
    }
    public static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new StringConfig(list, nameKey));
    }
    public static RangeLimitConfig addRangeLimitConfig(ILPCConfigList list, boolean defaultBoolean, String defaultPrefix){
        return list.addConfig(new RangeLimitConfig(list, defaultBoolean, defaultPrefix));
    }
    public static ColorConfig addColorConfig(ILPCConfigList list, @NotNull String nameKey, @NotNull Color4f defaultColor){
        return list.addConfig(new ColorConfig(list, nameKey, defaultColor));
    }
    public static ColorConfig addColorConfig(ILPCConfigList list, @NotNull String nameKey, @NotNull Color4f defaultColor, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new ColorConfig(list, nameKey, defaultColor, callback));
    }
    public static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new ArrayOptionListConfig<>(list, nameKey));
    }
    public static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(ILPCConfigList list, @NotNull String nameKey, ILPCValueChangeCallback callback){
        return list.addConfig(new ArrayOptionListConfig<>(list, nameKey, callback));
    }
    public static ConfigListListConfig addConfigListListConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new ConfigListListConfig(list, nameKey));
    }
    public static ConfigListListConfig addConfigListListConfig(ILPCConfigList list, @NotNull String nameKey, ILPCValueChangeCallback callback){
        return list.addConfig(new ConfigListListConfig(list, nameKey, callback));
    }
    public static ReachDistanceConfig addReachDistanceConfig(ILPCConfigList list){
        return list.addConfig(new ReachDistanceConfig(list));
    }
    public static ReachDistanceConfig addReachDistanceConfig(ILPCConfigList list, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new ReachDistanceConfig(list, callback));
    }

    //不带List版本的，使用栈存储当前list，方便操作
    public static final @NotNull Stack<ILPCConfigList> listStack = new Stack<>();
    public static ILPCConfigList peekConfigList(){return listStack.peek();}
    public static class ConfigListLayer implements Supplier<ILPCConfigList>, Consumer<ILPCConfigList>, AutoCloseable {
        public ConfigListLayer(){listStack.push(null);}
        @Override public void close(){listStack.pop();}
        @Override public void accept(ILPCConfigList list) {
            listStack.pop();
            listStack.push(list);
        }
        @Override public ILPCConfigList get() {
            return listStack.peek();
        }
        public <T extends ILPCConfigList> T set(T list){
            listStack.pop();
            listStack.push(list);
            return list;
        }
    }
    public static <T extends ILPCConfig> T addConfig(T config){return listStack.peek().addConfig(config);}
    public static BooleanConfig addBooleanConfig(@NotNull String nameKey, boolean defaultBoolean){
        return addBooleanConfig(peekConfigList(), nameKey, defaultBoolean);
    }
    public static BooleanConfig addBooleanConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback){
        return addBooleanConfig(peekConfigList(), nameKey, defaultBoolean, callback);
    }
    public static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger);
    }
    public static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, @Nullable ILPCValueChangeCallback callback){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger, callback);
    }
    public static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, int minValue, int maxValue){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger, minValue, maxValue);
    }
    public static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, int minValue, int maxValue, @Nullable ILPCValueChangeCallback callback){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger, minValue, maxValue, callback);
    }
    public static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble);
    }
    public static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, @Nullable ILPCValueChangeCallback callback){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble, callback);
    }
    public static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, double minValue, double maxValue){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble, minValue, maxValue);
    }
    public static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, double minValue, double maxValue, @Nullable ILPCValueChangeCallback callback){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble, minValue, maxValue, callback);
    }
    public static HotkeyConfig addHotkeyConfig(@NotNull String nameKey, @Nullable String defaultStorageString, @NotNull IHotkeyCallback callBack){
        return addHotkeyConfig(peekConfigList(), nameKey, defaultStorageString, callBack);
    }
    public static BooleanHotkeyConfig addBooleanHotkeyConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        return addBooleanHotkeyConfig(peekConfigList(), nameKey, defaultBoolean, defaultStorageString);
    }
    public static BooleanHotkeyConfig addBooleanHotkeyConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback){
        return addBooleanHotkeyConfig(peekConfigList(), nameKey, defaultBoolean, defaultStorageString, callback);
    }
    public static StringListConfig addStringListConfig(@NotNull String nameKey, @Nullable ImmutableList<String> defaultValue){
        return addStringListConfig(peekConfigList(), nameKey, defaultValue);
    }
    public static StringListConfig addStringListConfig(@NotNull String nameKey, @Nullable ImmutableList<String> defaultValue, @Nullable ILPCValueChangeCallback callback){
        return addStringListConfig(peekConfigList(), nameKey, defaultValue, callback);
    }
    public static ConfigOpenGuiConfig addConfigOpenGuiConfig(@Nullable String defaultStorageString){
        return addConfigOpenGuiConfig(peekConfigList(), defaultStorageString);
    }
    public static ThirdListConfig addThirdListConfig(@NotNull String nameKey, boolean defaultBoolean){
        return addThirdListConfig(peekConfigList(), nameKey, defaultBoolean);
    }
    public static <T> OptionListConfig addOptionListConfig(@NotNull String nameKey, IConfigOptionListEntry defaultOption){
        return addOptionListConfig(peekConfigList(), nameKey, defaultOption);
    }
    public static <T> OptionListConfig addOptionListConfig(@NotNull String nameKey, IConfigOptionListEntry defaultOption, @Nullable ILPCValueChangeCallback callback){
        return addOptionListConfig(peekConfigList(), nameKey, defaultOption, callback);
    }
    public static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(@NotNull String nameKey, Iterable<T> values){
        return addIntegerListConfig(peekConfigList(), nameKey, values);
    }
    public static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(@NotNull String nameKey, Iterable<T> values, @Nullable ILPCValueChangeCallback callback){
        return addIntegerListConfig(peekConfigList(), nameKey, values);
    }
    public static StringConfig addStringConfig(@NotNull String nameKey, @Nullable String defaultString, @Nullable ILPCValueChangeCallback callback){
        return addStringConfig(peekConfigList(), nameKey, defaultString, callback);
    }
    public static StringConfig addStringConfig(@NotNull String nameKey, @Nullable String defaultString){
        return addStringConfig(peekConfigList(), nameKey, defaultString);
    }
    public static StringConfig addStringConfig(@NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        return addStringConfig(peekConfigList(), nameKey, callback);
    }
    public static StringConfig addStringConfig(@NotNull String nameKey){
        return addStringConfig(peekConfigList(), nameKey);
    }
    public static RangeLimitConfig addRangeLimitConfig(boolean defaultBoolean, String defaultPrefix){
        return addRangeLimitConfig(peekConfigList(), defaultBoolean, defaultPrefix);
    }
    public static ColorConfig addColorConfig(@NotNull String nameKey, @NotNull Color4f defaultColor){
        return addColorConfig(peekConfigList(), nameKey, defaultColor);
    }
    public static ColorConfig addColorConfig(@NotNull String nameKey, @NotNull Color4f defaultColor, @Nullable ILPCValueChangeCallback callback){
        return addColorConfig(peekConfigList(), nameKey, defaultColor, callback);
    }
    public static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(@NotNull String nameKey){
        return addArrayOptionListConfig(peekConfigList(), nameKey);
    }
    public static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(@NotNull String nameKey, ILPCValueChangeCallback callback){
        return addArrayOptionListConfig(peekConfigList(), nameKey, callback);
    }
    public static ConfigListListConfig addConfigListListConfig(@NotNull String nameKey){
        return addConfigListListConfig(peekConfigList(), nameKey);
    }
    public static ConfigListListConfig addConfigListListConfig(@NotNull String nameKey, ILPCValueChangeCallback callback){
        return addConfigListListConfig(peekConfigList(), nameKey, callback);
    }
    public static ReachDistanceConfig addReachDistanceConfig(){
        return addReachDistanceConfig(peekConfigList());
    }
    public static ReachDistanceConfig addReachDistanceConfig(@Nullable ILPCValueChangeCallback callback){
        return addReachDistanceConfig(peekConfigList(), callback);
    }
}
