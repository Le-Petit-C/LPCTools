package lpctools.lpcfymasaapi.configbutton.transferredConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.interfaces.IButtonDisplay;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPC_MASAConfigWrapper;
import lpctools.lpcfymasaapi.interfaces.data.LPCConfigData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class HotkeyConfig extends ConfigHotkey implements ILPC_MASAConfigWrapper<ConfigHotkey> {
    public HotkeyConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable String defaultStorageString, @Nullable IHotkeyCallback hotkeyCallback){
        super(nameKey, defaultStorageString != null ? defaultStorageString : "");
        data = new LPCConfigData(parent, true);
        ILPC_MASAConfigWrapperDefaultInit(null);
        parent.getPage().getInputHandler().addHotkey(this);
        getKeybind().setCallback(hotkeyCallback);
    }
    
    public HotkeyConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey, @Nullable String defaultStorageString){
        this(parent, nameKey, defaultStorageString, null);
    }

    @SuppressWarnings("unused")
    public static class IntegerChanger<T extends IntSupplier & IntConsumer & IButtonDisplay> implements IHotkeyCallback{
        public IntegerChanger(int changeValue, @NotNull T valueToChange){
            this(changeValue, valueToChange, null);
        }
        public IntegerChanger(int changeValue, @NotNull T valueToChange, @Nullable BooleanSupplier enabled){
            this.changeValue = changeValue;
            this.valueToChange = valueToChange;
            this.enabled = enabled;
        }
        @Override public boolean onKeyAction(KeyAction action, IKeybind key){
            if(enabled != null && !enabled.getAsBoolean()) return false;
            valueToChange.accept(valueToChange.getAsInt() + changeValue);
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if(player != null) player.sendMessage(Text.of(
                    StringUtils.translate("lpcfymalilib.hotkeyValueDisplay", valueToChange.getDisplayName().trim()) + " " + valueToChange.getAsInt()
                    ), true);
            return true;
        }
        private final int changeValue;
        @NotNull private final T valueToChange;
        @Nullable private final BooleanSupplier enabled;
    }

    @Override public void setValueFromJsonElement(@NotNull JsonElement element) {
        List<Integer> lastKeys = List.copyOf(getKeybind().getKeys());
        super.setValueFromJsonElement(element);
        if(!lastKeys.equals(getKeybind().getKeys())) onValueChanged();
    }
    @Override public @NotNull LPCConfigData getLPCConfigData() {return data;}
    private final @NotNull LPCConfigData data;
}
