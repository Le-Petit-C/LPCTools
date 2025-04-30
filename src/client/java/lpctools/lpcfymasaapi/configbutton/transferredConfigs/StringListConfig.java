package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import lpctools.lpcfymasaapi.configbutton.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.configbutton.ILPC_MASAConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class StringListConfig extends ConfigStringList implements ILPC_MASAConfigWrapper<ConfigStringList>, Iterable<String> {
    public StringListConfig(@NotNull ILPCConfigList defaultParent, String nameKey, @Nullable ImmutableList<String> defaultValue){
        this(defaultParent, nameKey, defaultValue, null);
    }
    public StringListConfig(@NotNull ILPCConfigList defaultParent, String nameKey, @Nullable ImmutableList<String> defaultValue, ILPCValueChangeCallback callback){
        super(nameKey, defaultValue);
        data = new Data(defaultParent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public @NotNull Iterator<String> iterator() {return getStrings().iterator();}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
