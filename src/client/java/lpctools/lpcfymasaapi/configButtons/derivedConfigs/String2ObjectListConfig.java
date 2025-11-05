package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import com.google.common.collect.ImmutableList;
import lpctools.lpcfymasaapi.LPCAPIInit;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.function.Function;

import static lpctools.util.AlgorithmUtils.*;
import static lpctools.util.DataUtils.*;

public class String2ObjectListConfig<T> extends UniqueStringListConfig {
    public final HashSet<T> set = new HashSet<>();
    private final Function<String, T> converter;
    public String2ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, defaultValue);
        this.converter = converter;
        getStrings().forEach(str->set.add(converter.apply(str)));
        setValueChangeCallback(callback);
    }
    public String2ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends T> defaultValue, Function<String, T> converter, Function<T, String> backConverter, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, convertToImmutableList(defaultValue, backConverter));
        this.converter = converter;
        if(defaultValue != null) defaultValue.forEach(set::add);
        setValueChangeCallback(callback);
    }
    public String2ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable ImmutableList<String> defaultValue, Function<String, T> converter) {
        this(parent, nameKey, defaultValue, converter, null);
    }
    public String2ObjectListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<T> defaultValue, Function<String, T> converter, Function<T, String> backConverter) {
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
    
}
