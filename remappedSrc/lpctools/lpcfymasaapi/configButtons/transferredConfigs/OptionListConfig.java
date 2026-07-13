package lpctools.lpcfymasaapi.configButtons.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.IConfigOptionListEntry;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OptionListConfig extends ConfigOptionList implements ILPC_MASAConfigWrapper<ConfigOptionList>, IButtonDisplay{
    public OptionListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull IConfigOptionListEntry defaultValue) {
        this(parent, nameKey, defaultValue, null);
    }
    public OptionListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull IConfigOptionListEntry defaultValue, @Nullable ILPCValueChangeCallback callback) {
        super(nameKey, defaultValue);
        data = new LPCConfigData(parent, false);
        ILPC_MASAConfigWrapperDefaultInit(callback);
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        ILPC_MASAConfigWrapper.super.setValueFromJsonElement(element);
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        String lastString = getStringValue();
        super.setValueFromJsonElement(element);
        return new UpdateTodo().valueChanged(!lastString.equals(getStringValue()));
    }
    @Override @NotNull public String getDisplayName(){return getOptionListValue().getDisplayName();}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
