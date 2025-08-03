package lpctools.lpcfymasaapi.configButtons.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigBooleanHotkeyed;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;

public class BooleanHotkeyConfig extends ConfigBooleanHotkeyed implements ILPC_MASAConfigWrapper<ConfigBoolean>, BooleanSupplier, BooleanConsumer {
    public BooleanHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString){
        this(parent, nameKey, defaultBoolean, defaultStorageString, null);
    }
    public BooleanHotkeyConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, boolean defaultBoolean, @Nullable String defaultStorageString, @Nullable ILPCValueChangeCallback callback){
        super(nameKey, defaultBoolean, defaultStorageString == null ? "" : defaultStorageString, "");
        data = new LPCConfigData(parent, true);
        ILPC_MASAConfigWrapperDefaultInit(callback);
        parent.getPage().getInputHandler().addHotkey(this);
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        ILPC_MASAConfigWrapper.super.setValueFromJsonElement(element);
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        boolean lastBoolean = getAsBoolean();
        List<Integer> lastKeys = List.copyOf(getKeybind().getKeys());
        super.setValueFromJsonElement(element);
        return new UpdateTodo().valueChanged(lastBoolean != getAsBoolean() || !lastKeys.equals(getKeybind().getKeys()));
    }
    @Override public boolean getAsBoolean() {return getBooleanValue();}
    @Override public void accept(boolean b) {setBooleanValue(b);}
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
