package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.LPCAPIInit;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.DataUtils;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.function.Function;

import static lpctools.util.AlgorithmUtils.*;
import static lpctools.util.DataUtils.*;

public class ObjectListConfig<T> extends UniqueStringListConfig {
    public final HashSet<T> set = new HashSet<>();
    private final Function<String, T> converter;
    public ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, defaultValue);
        this.converter = converter;
        getStrings().forEach(str->set.add(converter.apply(str)));
        setValueChangeCallback(callback);
    }
    public ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends T> defaultValue, Function<String, T> converter, Function<T, String> backConverter, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, convertToImmutableList(defaultValue, backConverter));
        this.converter = converter;
        if(defaultValue != null) defaultValue.forEach(set::add);
        setValueChangeCallback(callback);
    }
    public ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter) {
        this(parent, nameKey, defaultValue, converter, null);
    }
    public ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<T> defaultValue, Function<String, T> converter, Function<T, String> backConverter) {
        this(parent, nameKey, defaultValue, converter, backConverter, null);
    }
    @Override public void onValueChanged() {refresh();}
    public void refresh(){
        HashSet<T> set = new HashSet<>();
        getStrings().forEach(str->{
            T v = converter.apply(str);
            if(set.add(v)) return;
            notifyPlayer(String.format("§e%s duplicates.", str), false);
            LPCAPIInit.LOGGER.warn("{} duplicates.", str);
        });
        if(set.equals(this.set)) return;
        this.set.clear();
        this.set.addAll(set);
        super.onValueChanged();
    }
    public boolean contains(T o){return set.contains(o);}
    
    public static class ItemListConfig extends ObjectListConfig<Item> {
        public static final Function<String, Item> converter = id->getItemFromId(id, true);
        public static final Function<Item, String> backConverter = DataUtils::getItemId;
        public ItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<Item> defaultValue, @Nullable ILPCValueChangeCallback callback) {
            super(parent, nameKey, defaultValue, converter, backConverter, callback);
        }
        public ItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<Item> defaultValue) {
            this(parent, nameKey, defaultValue, null);
        }
    }
    public static class BlockListConfig extends ObjectListConfig<Block> {
        public static final Function<String, Block> converter = id->getBlockFromId(id, true);
        public static final Function<Block, String> backConverter = DataUtils::getBlockId;
        public BlockListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends Block> defaultValue, @Nullable ILPCValueChangeCallback callback) {
            super(parent, nameKey, defaultValue, converter, backConverter, callback);
        }
        public BlockListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends Block> defaultValue) {
            this(parent, nameKey, defaultValue, null);
        }
        public void setBlocks(Iterable<Block> blocks){
            setStrings(DataUtils.idListFromBlockList(blocks));
        }
    }
    public static class BlockItemListConfig extends ObjectListConfig<BlockItem> {
        public static final Function<String, BlockItem> converter = id->getBlockItemFromId(id, true);
        public static final Function<BlockItem, String> backConverter = DataUtils::getItemId;
        public BlockItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<BlockItem> defaultValue, @Nullable ILPCValueChangeCallback callback) {
            super(parent, nameKey, defaultValue, converter, backConverter, callback);
        }
        public BlockItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<BlockItem> defaultValue) {
            this(parent, nameKey, defaultValue, null);
        }
    }
}
