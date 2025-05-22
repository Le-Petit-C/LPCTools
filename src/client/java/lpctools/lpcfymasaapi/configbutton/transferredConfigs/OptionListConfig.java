package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import lpctools.lpcfymasaapi.implementations.IButtonDisplay;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.implementations.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.implementations.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OptionListConfig extends ConfigOptionList implements ILPC_MASAConfigWrapper<ConfigOptionList>, IButtonDisplay{
    public OptionListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @NotNull IConfigOptionListEntry defaultValue) {
        this(parent, nameKey, defaultValue, null);
    }
    public OptionListConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @NotNull IConfigOptionListEntry defaultValue, @Nullable ILPCValueChangeCallback callback) {
        super(nameKey, defaultValue);
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement element) {
        String lastString = getStringValue();
        super.setValueFromJsonElement(element);
        if(!lastString.equals(getStringValue())) onValueChanged();
    }
    @Override @NotNull public String getDisplayName(){return getOptionListValue().getDisplayName();}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
