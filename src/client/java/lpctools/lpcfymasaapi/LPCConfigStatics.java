package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import fi.dy.masa.malilib.util.data.Color4f;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.*;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.*;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.*;
import lpctools.lpcfymasaapi.interfaces.*;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Stack;
import java.util.function.*;

@SuppressWarnings({"unused", "UnusedReturnValue", "resource"})
public interface LPCConfigStatics {
    //加入的配置无法删除
    //加入的配置不会立刻从文件中加载已有的设定，直到loadFromConfigPageJson被调用（一般来自LPCConfigPage中的load()），
    //一般情况下是malilib初始化时或者加入了一个世界时malilib会调用load()，此时会从文件中加载所有保存的配置
    static BooleanConfig addBooleanConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean){
        return list.addConfig(new BooleanConfig(list, nameKey, defaultBoolean));
    }
    static BooleanConfig addBooleanConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new BooleanConfig(list, nameKey, defaultBoolean, callback));
    }
    static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger));
    }
    static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger, callback));
    }
    static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger, int minValue, int maxValue){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger, minValue, maxValue));
    }
    static IntegerConfig addIntegerConfig(ILPCConfigList list, @NotNull String nameKey, int defaultInteger, int minValue, int maxValue, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new IntegerConfig(list, nameKey, defaultInteger, minValue, maxValue, callback));
    }
    static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble));
    }
    static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble, callback));
    }
    static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble, double minValue, double maxValue){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble, minValue, maxValue));
    }
    static DoubleConfig addDoubleConfig(ILPCConfigList list, @NotNull String nameKey, double defaultDouble, double minValue, double maxValue, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new DoubleConfig(list, nameKey, defaultDouble, minValue, maxValue, callback));
    }
    static HotkeyConfig addHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, @NotNull KeybindSettings defaultKeybindSettings, @Nullable String defaultStorageString, @Nullable IHotkeyCallback callBack){
        return list.addConfig(new HotkeyConfig(list, nameKey, defaultKeybindSettings, defaultStorageString, callBack));
    }
    static HotkeyConfig addHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultStorageString, @Nullable IHotkeyCallback callBack){
        return list.addConfig(new HotkeyConfig(list, nameKey, defaultStorageString, callBack));
    }
    static HotkeyConfig addHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultStorageString){
        return list.addConfig(new HotkeyConfig(list, nameKey, defaultStorageString));
    }
    static BooleanHotkeyConfig addBooleanHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        return list.addConfig(new BooleanHotkeyConfig(list, nameKey, defaultBoolean, defaultStorageString));
    }
    static BooleanHotkeyConfig addBooleanHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new BooleanHotkeyConfig(list, nameKey, defaultBoolean, defaultStorageString, callback));
    }
    static StringListConfig addStringListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ImmutableList<String> defaultValue){
        return list.addConfig(new StringListConfig(list, nameKey, defaultValue));
    }
    static StringListConfig addStringListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ImmutableList<String> defaultValue, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new StringListConfig(list, nameKey, defaultValue, callback));
    }
    static ConfigOpenGuiConfig addConfigOpenGuiConfig(ILPCConfigList list, @Nullable String defaultStorageString){
        return list.addConfig(new ConfigOpenGuiConfig(list, defaultStorageString));
    }
    static ThirdListConfig addThirdListConfig(ILPCConfigList list, @NotNull String nameKey, ILPCValueChangeCallback callback){
        return list.addConfig(new ThirdListConfig(list, nameKey, callback));
    }
    static BooleanThirdListConfig addBooleanThirdListConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, ILPCValueChangeCallback callback){
        return list.addConfig(new BooleanThirdListConfig(list, nameKey, defaultBoolean, callback));
    }
    static <T> OptionListConfig addOptionListConfig(ILPCConfigList list, @NotNull String nameKey, IConfigOptionListEntry defaultOption){
        return list.addConfig(new OptionListConfig(list, nameKey, defaultOption));
    }
    static <T> OptionListConfig addOptionListConfig(ILPCConfigList list, @NotNull String nameKey, IConfigOptionListEntry defaultOption, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new OptionListConfig(list, nameKey, defaultOption, callback));
    }
    static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(ILPCConfigList list, @NotNull String nameKey, Iterable<T> values){
        return list.addConfig(new IntegerArrayListConfig<>(list, nameKey, values));
    }
    static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(ILPCConfigList list, @NotNull String nameKey, Iterable<T> values, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new IntegerArrayListConfig<>(list, nameKey, values));
    }
    static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultString, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new StringConfig(list, nameKey, defaultString, callback));
    }
    static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultString){
        return list.addConfig(new StringConfig(list, nameKey, defaultString));
    }
    static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new StringConfig(list, nameKey, callback));
    }
    static StringConfig addStringConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new StringConfig(list, nameKey));
    }
    static RangeLimitConfig addRangeLimitConfig(ILPCConfigList list, String defaultPrefix){
        return list.addConfig(new RangeLimitConfig(list, defaultPrefix));
    }
    static ColorConfig addColorConfig(ILPCConfigList list, @NotNull String nameKey, @NotNull Color4f defaultColor){
        return list.addConfig(new ColorConfig(list, nameKey, defaultColor));
    }
    static ColorConfig addColorConfig(ILPCConfigList list, @NotNull String nameKey, @NotNull Color4f defaultColor, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new ColorConfig(list, nameKey, defaultColor, callback));
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new ArrayOptionListConfig<>(list, nameKey));
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(ILPCConfigList list, @NotNull String nameKey, ILPCValueChangeCallback callback){
        return list.addConfig(new ArrayOptionListConfig<>(list, nameKey, callback));
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(ILPCConfigList list, @NotNull String nameKey, Map<String, ? extends T> values){
        return list.addConfig(new ArrayOptionListConfig<>(list, nameKey, values));
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(ILPCConfigList list, @NotNull String nameKey, Map<String, ? extends T> values, ILPCValueChangeCallback callback){
        return list.addConfig(new ArrayOptionListConfig<>(list, nameKey, values, callback));
    }
    static ConfigListOptionListConfig addConfigListOptionListConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new ConfigListOptionListConfig(list, nameKey));
    }
    static ConfigListOptionListConfig addConfigListOptionListConfig(ILPCConfigList list, @NotNull String nameKey, ILPCValueChangeCallback callback){
        return list.addConfig(new ConfigListOptionListConfig(list, nameKey, callback));
    }
    static ReachDistanceConfig addReachDistanceConfig(ILPCConfigList list){
        return list.addConfig(new ReachDistanceConfig(list));
    }
    static ReachDistanceConfig addReachDistanceConfig(ILPCConfigList list, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new ReachDistanceConfig(list, callback));
    }
    static LimitOperationSpeedConfig addLimitOperationSpeedConfig(ILPCConfigList list, boolean defaultBoolean, double defaultDouble){
        return list.addConfig(new LimitOperationSpeedConfig(list, defaultBoolean, defaultDouble));
    }
    static <T> ConfigListOptionListConfigEx<T> addConfigListOptionListConfigEx(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new ConfigListOptionListConfigEx<>(list, nameKey));
    }
    static <T> ConfigListOptionListConfigEx<T> addConfigListOptionListConfigEx(ILPCConfigList list, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new ConfigListOptionListConfigEx<>(list, nameKey, callback));
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new String2ObjectListConfig<>(list, nameKey, defaultValue, converter, callback));
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<T> defaultValue, Function<String, T> converter, Function<T, String> backConverter, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new String2ObjectListConfig<>(list, nameKey, defaultValue, converter, backConverter, callback));
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter){
        return list.addConfig(new String2ObjectListConfig<>(list, nameKey, defaultValue, converter));
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<T> defaultValue, Function<String, T> converter, Function<T, String> backConverter){
        return list.addConfig(new String2ObjectListConfig<>(list, nameKey, defaultValue, converter, backConverter));
    }
    static ItemListConfig addItemListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<Item> defaultValue, ILPCValueChangeCallback callback){
        return list.addConfig(new ItemListConfig(list, nameKey, defaultValue, callback));
    }
    static ItemListConfig addItemListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<Item> defaultValue){
        return list.addConfig(new ItemListConfig(list, nameKey, defaultValue));
    }
    static BlockListConfig addBlockListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<? extends Block> defaultValue, ILPCValueChangeCallback callback){
        return list.addConfig(new BlockListConfig(list, nameKey, defaultValue, callback));
    }
    static BlockListConfig addBlockListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<? extends Block> defaultValue){
        return list.addConfig(new BlockListConfig(list, nameKey, defaultValue));
    }
    static BlockItemListConfig addBlockItemListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<BlockItem> defaultValue, ILPCValueChangeCallback callback){
        return list.addConfig(new BlockItemListConfig(list, nameKey, defaultValue, callback));
    }
    static BlockItemListConfig addBlockItemListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable Iterable<BlockItem> defaultValue){
        return list.addConfig(new BlockItemListConfig(list, nameKey, defaultValue));
    }
    static ButtonConfig addButtonConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable IButtonActionListener buttonActionListener){
        return list.addConfig(new ButtonConfig(list, nameKey, buttonActionListener));
    }
    static ButtonConfig addButtonConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new ButtonConfig(list, nameKey));
    }
    static BooleanHotkeyThirdListConfig addBooleanHotkeyThirdListConfig(ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultHotkey, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new BooleanHotkeyThirdListConfig(list, nameKey, defaultBoolean, defaultHotkey, callback));
    }
    static BooleanHotkeyThirdListConfig addBooleanHotkeyThirdListConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new BooleanHotkeyThirdListConfig(list, nameKey, callback));
    }
    static BooleanHotkeyThirdListConfig addBooleanHotkeyThirdListConfig(ILPCConfigList list, @NotNull String nameKey){
        return list.addConfig(new BooleanHotkeyThirdListConfig(list, nameKey));
    }
    static ButtonHotkeyConfig addButtonHotkeyConfig(ILPCConfigList list, @NotNull String nameKey, @Nullable String defaultKeyBindStorageString, @Nullable Runnable callback){
        return list.addConfig(new ButtonHotkeyConfig(list, nameKey, defaultKeyBindStorageString, callback));
    }
    static BlockPosConfig addBlockPosConfig(ILPCConfigList list, @NotNull String nameKey, BlockPos defaultPos, @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new BlockPosConfig(list, nameKey, defaultPos, callback));
    }
    static <T extends ILPCUniqueConfigBase> MutableConfig<T> addMutableConfig(@NotNull ILPCConfigList list, @NotNull String nameKey, @NotNull String buttonKeyPrefix,
                                                                              @NotNull ImmutableMap<String, BiFunction<MutableConfig<T>, String, T>> configSuppliers,
                                                                              @Nullable ILPCValueChangeCallback callback){
        return list.addConfig(new MutableConfig<>(list, nameKey, buttonKeyPrefix, configSuppliers, callback));
    }

    //不带List版本的，使用栈存储当前list，方便操作
    @NotNull Stack<ILPCConfigList> listStack = new Stack<>();
    static ILPCConfigList peekConfigList(){return listStack.peek();}
	class ConfigListLayer implements Supplier<ILPCConfigList>, Consumer<ILPCConfigList>, AutoCloseable {
        public ConfigListLayer(ILPCConfigList parent){listStack.push(parent);}
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
    static <T extends ILPCConfig> T addConfig(T config){return listStack.peek().addConfig(config);}
    static BooleanConfig addBooleanConfig(@NotNull String nameKey, boolean defaultBoolean){
        return addBooleanConfig(peekConfigList(), nameKey, defaultBoolean);
    }
    static BooleanConfig addBooleanConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable ILPCValueChangeCallback callback){
        return addBooleanConfig(peekConfigList(), nameKey, defaultBoolean, callback);
    }
    static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger);
    }
    static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, @Nullable ILPCValueChangeCallback callback){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger, callback);
    }
    static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, int minValue, int maxValue){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger, minValue, maxValue);
    }
    static IntegerConfig addIntegerConfig(@NotNull String nameKey, int defaultInteger, int minValue, int maxValue, @Nullable ILPCValueChangeCallback callback){
        return addIntegerConfig(peekConfigList(), nameKey, defaultInteger, minValue, maxValue, callback);
    }
    static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble);
    }
    static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, @Nullable ILPCValueChangeCallback callback){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble, callback);
    }
    static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, double minValue, double maxValue){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble, minValue, maxValue);
    }
    static DoubleConfig addDoubleConfig(@NotNull String nameKey, double defaultDouble, double minValue, double maxValue, @Nullable ILPCValueChangeCallback callback){
        return addDoubleConfig(peekConfigList(), nameKey, defaultDouble, minValue, maxValue, callback);
    }
    static HotkeyConfig addHotkeyConfig(@NotNull String nameKey, KeybindSettings defaultKeybindSettings, @Nullable String defaultStorageString, @Nullable IHotkeyCallback callBack){
        return addHotkeyConfig(peekConfigList(), nameKey, defaultKeybindSettings, defaultStorageString, callBack);
    }
    static HotkeyConfig addHotkeyConfig(@NotNull String nameKey, @Nullable String defaultStorageString, @NotNull IHotkeyCallback callBack){
        return addHotkeyConfig(peekConfigList(), nameKey, defaultStorageString, callBack);
    }
    static HotkeyConfig addHotkeyConfig(@NotNull String nameKey, @Nullable String defaultStorageString){
        return addHotkeyConfig(peekConfigList(), nameKey, defaultStorageString);
    }
    static BooleanHotkeyConfig addBooleanHotkeyConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        return addBooleanHotkeyConfig(peekConfigList(), nameKey, defaultBoolean, defaultStorageString);
    }
    static BooleanHotkeyConfig addBooleanHotkeyConfig(@NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback){
        return addBooleanHotkeyConfig(peekConfigList(), nameKey, defaultBoolean, defaultStorageString, callback);
    }
    static StringListConfig addStringListConfig(@NotNull String nameKey, @Nullable ImmutableList<String> defaultValue){
        return addStringListConfig(peekConfigList(), nameKey, defaultValue);
    }
    static StringListConfig addStringListConfig(@NotNull String nameKey, @Nullable ImmutableList<String> defaultValue, @Nullable ILPCValueChangeCallback callback){
        return addStringListConfig(peekConfigList(), nameKey, defaultValue, callback);
    }
    static ConfigOpenGuiConfig addConfigOpenGuiConfig(@Nullable String defaultStorageString){
        return addConfigOpenGuiConfig(peekConfigList(), defaultStorageString);
    }
    static ThirdListConfig addThirdListConfig(@NotNull String nameKey, ILPCValueChangeCallback callback){
        return addThirdListConfig(peekConfigList(), nameKey, callback);
    }
    static BooleanThirdListConfig addBooleanThirdListConfig(@NotNull String nameKey, boolean defaultBoolean, ILPCValueChangeCallback callback){
        return addBooleanThirdListConfig(peekConfigList(), nameKey, defaultBoolean, callback);
    }
    static <T> OptionListConfig addOptionListConfig(@NotNull String nameKey, IConfigOptionListEntry defaultOption){
        return addOptionListConfig(peekConfigList(), nameKey, defaultOption);
    }
    static <T> OptionListConfig addOptionListConfig(@NotNull String nameKey, IConfigOptionListEntry defaultOption, @Nullable ILPCValueChangeCallback callback){
        return addOptionListConfig(peekConfigList(), nameKey, defaultOption, callback);
    }
    static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(@NotNull String nameKey, Iterable<T> values){
        return addIntegerListConfig(peekConfigList(), nameKey, values);
    }
    static <T extends IntSupplier & IntConsumer> IntegerArrayListConfig<T> addIntegerListConfig(@NotNull String nameKey, Iterable<T> values, @Nullable ILPCValueChangeCallback callback){
        return addIntegerListConfig(peekConfigList(), nameKey, values);
    }
    static StringConfig addStringConfig(@NotNull String nameKey, @Nullable String defaultString, @Nullable ILPCValueChangeCallback callback){
        return addStringConfig(peekConfigList(), nameKey, defaultString, callback);
    }
    static StringConfig addStringConfig(@NotNull String nameKey, @Nullable String defaultString){
        return addStringConfig(peekConfigList(), nameKey, defaultString);
    }
    static StringConfig addStringConfig(@NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        return addStringConfig(peekConfigList(), nameKey, callback);
    }
    static StringConfig addStringConfig(@NotNull String nameKey){
        return addStringConfig(peekConfigList(), nameKey);
    }
    static RangeLimitConfig addRangeLimitConfig(String defaultPrefix){
        return addRangeLimitConfig(peekConfigList(), defaultPrefix);
    }
    static RangeLimitConfig addRangeLimitConfig(){
        return addRangeLimitConfig(peekConfigList(), peekConfigList().getNameKey());
    }
    static ColorConfig addColorConfig(@NotNull String nameKey, @NotNull Color4f defaultColor){
        return addColorConfig(peekConfigList(), nameKey, defaultColor);
    }
    static ColorConfig addColorConfig(@NotNull String nameKey, @NotNull Color4f defaultColor, @Nullable ILPCValueChangeCallback callback){
        return addColorConfig(peekConfigList(), nameKey, defaultColor, callback);
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(@NotNull String nameKey){
        return addArrayOptionListConfig(peekConfigList(), nameKey);
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(@NotNull String nameKey, ILPCValueChangeCallback callback){
        return addArrayOptionListConfig(peekConfigList(), nameKey, callback);
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(@NotNull String nameKey, Map<String, ? extends T> values){
        return addArrayOptionListConfig(peekConfigList(), nameKey, values);
    }
    static <T> ArrayOptionListConfig<T> addArrayOptionListConfig(@NotNull String nameKey, Map<String, ? extends T> values, ILPCValueChangeCallback callback){
        return addArrayOptionListConfig(peekConfigList(), nameKey, values, callback);
    }
    static ConfigListOptionListConfig addConfigListOptionListConfig(@NotNull String nameKey){
        return addConfigListOptionListConfig(peekConfigList(), nameKey);
    }
    static ConfigListOptionListConfig addConfigListOptionListConfig(@NotNull String nameKey, ILPCValueChangeCallback callback){
        return addConfigListOptionListConfig(peekConfigList(), nameKey, callback);
    }
    static ReachDistanceConfig addReachDistanceConfig(){
        return addReachDistanceConfig(peekConfigList());
    }
    static ReachDistanceConfig addReachDistanceConfig(@Nullable ILPCValueChangeCallback callback){
        return addReachDistanceConfig(peekConfigList(), callback);
    }
    static LimitOperationSpeedConfig addLimitOperationSpeedConfig(boolean defaultBoolean, double defaultDouble){
        return addLimitOperationSpeedConfig(peekConfigList(), defaultBoolean, defaultDouble);
    }
    static <T> ConfigListOptionListConfigEx<T> addConfigListOptionListConfigEx(@NotNull String nameKey){
        return addConfigListOptionListConfigEx(peekConfigList(), nameKey);
    }
    static <T> ConfigListOptionListConfigEx<T> addConfigListOptionListConfigEx(@NotNull String nameKey, @Nullable ILPCValueChangeCallback callback){
        return addConfigListOptionListConfigEx(peekConfigList(), nameKey, callback);
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter, @Nullable ILPCValueChangeCallback callback){
        return addObjectListConfig(peekConfigList(), nameKey, defaultValue, converter, callback);
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(String nameKey, @Nullable Iterable<T> defaultValue, Function<String, T> converter, Function<T, String> backConverter, @Nullable ILPCValueChangeCallback callback){
        return addObjectListConfig(peekConfigList(), nameKey, defaultValue, converter, backConverter, callback);
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter){
        return addObjectListConfig(peekConfigList(), nameKey, defaultValue, converter);
    }
    static <T> String2ObjectListConfig<T> addObjectListConfig(String nameKey, @Nullable Iterable<T> defaultValue, Function<String, T> converter, Function<T, String> backConverter){
        return addObjectListConfig(peekConfigList(), nameKey, defaultValue, converter, backConverter);
    }
    static ItemListConfig addItemListConfig(String nameKey, @Nullable Iterable<Item> defaultValue, ILPCValueChangeCallback callback){
        return addItemListConfig(peekConfigList(), nameKey, defaultValue, callback);
    }
    static ItemListConfig addItemListConfig(String nameKey, @Nullable Iterable<Item> defaultValue){
        return addItemListConfig(peekConfigList(), nameKey, defaultValue);
    }
    static BlockListConfig addBlockListConfig(String nameKey, @Nullable Iterable<? extends Block> defaultValue, ILPCValueChangeCallback callback){
        return addBlockListConfig(peekConfigList(), nameKey, defaultValue, callback);
    }
    static BlockListConfig addBlockListConfig(String nameKey, @Nullable Iterable<? extends Block> defaultValue){
        return addBlockListConfig(peekConfigList(), nameKey, defaultValue);
    }
    static BlockItemListConfig addBlockItemListConfig(String nameKey, @Nullable Iterable<BlockItem> defaultValue, ILPCValueChangeCallback callback){
        return addBlockItemListConfig(peekConfigList(), nameKey, defaultValue, callback);
    }
    static BlockItemListConfig addBlockItemListConfig(String nameKey, @Nullable Iterable<BlockItem> defaultValue){
        return addBlockItemListConfig(peekConfigList(), nameKey, defaultValue);
    }
    static ButtonConfig addButtonConfig(String nameKey, @Nullable IButtonActionListener buttonActionListener){
        return addButtonConfig(peekConfigList(), nameKey, buttonActionListener);
    }
    static ButtonConfig addButtonConfig(String nameKey){
        return addButtonConfig(peekConfigList(), nameKey);
    }
    static BooleanHotkeyThirdListConfig addBooleanHotkeyThirdListConfig(String nameKey, boolean defaultBoolean, @Nullable String defaultHotkey, @Nullable ILPCValueChangeCallback callback){
        return addBooleanHotkeyThirdListConfig(peekConfigList(), nameKey, defaultBoolean, defaultHotkey, callback);
    }
    static BooleanHotkeyThirdListConfig addBooleanHotkeyThirdListConfig(String nameKey, @Nullable ILPCValueChangeCallback callback){
        return addBooleanHotkeyThirdListConfig(peekConfigList(), nameKey, callback);
    }
    static BooleanHotkeyThirdListConfig addBooleanHotkeyThirdListConfig(String nameKey){
        return addBooleanHotkeyThirdListConfig(peekConfigList(), nameKey);
    }
    static ButtonHotkeyConfig addButtonHotkeyConfig(String nameKey, @Nullable String defaultKeyBindStorageString, @Nullable Runnable callback){
        return addButtonHotkeyConfig(peekConfigList(), nameKey, defaultKeyBindStorageString, callback);
    }
    static BlockPosConfig addBlockPosConfig(@NotNull String nameKey, BlockPos defaultPos, ILPCValueChangeCallback callback){
        return addBlockPosConfig(peekConfigList(), nameKey, defaultPos, callback);
    }
    static <T extends ILPCUniqueConfigBase> MutableConfig<T> addMutableConfig(@NotNull String nameKey, @NotNull String buttonKeyPrefix,
                                                                              @NotNull ImmutableMap<String, BiFunction<MutableConfig<T>, String, T>> configSuppliers,
                                                                              @Nullable ILPCValueChangeCallback callback){
        return addMutableConfig(peekConfigList(), nameKey, buttonKeyPrefix, configSuppliers, callback);
    }
}
