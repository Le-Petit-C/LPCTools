package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringListConfig extends ConfigStringList implements ILPC_MASAConfigWrapper<ConfigStringList>, Supplier<List<String>>, Consumer<List<String>>, Iterable<String> {
    public StringListConfig(@NotNull ILPCConfigList parent, String nameKey, @Nullable ImmutableList<String> defaultValue){
        this(parent, nameKey, defaultValue, null);
    }
    public StringListConfig(@NotNull ILPCConfigList parent, String nameKey, @Nullable ImmutableList<String> defaultValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultValue, "");
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }

    @Override public void setValueFromJsonElement(@NotNull JsonElement element) {
        List<String> lastStrings = List.copyOf(getStrings());
        super.setValueFromJsonElement(element);
        if(!lastStrings.equals(getStrings())) onValueChanged();
    }
    @Override public @NotNull Iterator<String> iterator() {return getStrings().iterator();}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
    @Override public List<String> get() {return getStrings();}
    @Override public void accept(List<String> obj){setStrings(obj);}
}
