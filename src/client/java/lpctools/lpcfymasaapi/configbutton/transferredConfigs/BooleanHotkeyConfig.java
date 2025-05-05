package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lpctools.lpcfymasaapi.configbutton.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

public class BooleanHotkeyConfig extends ConfigBooleanHotkeyed implements ILPC_MASAConfigWrapper<ConfigBoolean>, BooleanSupplier, BooleanConsumer {
    public BooleanHotkeyConfig(@NotNull ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        this(list, nameKey, defaultBoolean, defaultStorageString, null);
    }
    public BooleanHotkeyConfig(@NotNull ILPCConfigList list, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, defaultBoolean, defaultStorageString == null ? "" : defaultStorageString);
        data = new Data(list, true);
        ILPC_MASAConfigWrapperDefaultInit(callback);
        list.getPage().getInputHandler().addHotkey(this);
    }
    @Override public void setValueFromJsonElement(JsonElement element) {
        boolean lastBoolean = getAsBoolean();
        List<Integer> lastKeys = List.copyOf(getKeybind().getKeys());
        super.setValueFromJsonElement(element);
        if(lastBoolean != getAsBoolean() || !lastKeys.equals(getKeybind().getKeys()))
            onValueChanged();
    }
    @Override public boolean getAsBoolean() {return getBooleanValue();}
    @Override public void accept(boolean b) {setBooleanValue(b);}
    @Override public @NotNull Data getLPCConfigData() {return data;}
    private final @NotNull Data data;
}
