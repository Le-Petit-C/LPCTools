package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringListConfig extends ConfigStringList implements ILPC_MASAConfigWrapper<ConfigStringList>, Supplier<List<String>>, Consumer<List<String>>, Iterable<String> {
    public StringListConfig(@NotNull ILPCConfigList defaultParent, String nameKey, @Nullable ImmutableList<String> defaultValue){
        this(defaultParent, nameKey, defaultValue, null);
    }
    public StringListConfig(@NotNull ILPCConfigList defaultParent, String nameKey, @Nullable ImmutableList<String> defaultValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultValue);
        data = new Data(defaultParent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }

    @Override public void setValueFromJsonElement(JsonElement element) {
        List<String> lastStrings = List.copyOf(getStrings());
        super.setValueFromJsonElement(element);
        if(!lastStrings.equals(getStrings())) onValueChanged();
    }
    @Override public @NotNull Iterator<String> iterator() {return getStrings().iterator();}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
    @Override public List<String> get() {return getStrings();}
    @Override public void accept(List<String> obj){setStrings(obj);}
}
