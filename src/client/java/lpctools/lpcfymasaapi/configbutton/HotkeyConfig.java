package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.LPCConfigList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class HotkeyConfig extends LPCConfig<ConfigHotkey> implements ILPCHotkey{
    @Nullable public final String defaultStorageString;
    @NotNull public final IHotkeyCallback hotkeyCallback;
    public HotkeyConfig(@NotNull LPCConfigList list, @NotNull String nameKey, @Nullable String defaultStorageString, @NotNull IHotkeyCallback hotkeyCallback){
        super(list, nameKey, true);
        this.defaultStorageString = defaultStorageString;
        this.hotkeyCallback = hotkeyCallback;
        list.getPage().getInputHandler().addHotkey(this);
    }
    @Override public IHotkey LPCGetHotkey() {return getConfig();}

    @Override @NotNull protected ConfigHotkey createInstance(){
        ConfigHotkey config = new ConfigHotkey(getFullNameTranslationKey(), defaultStorageString, getFullCommentTranslationKey());
        config.getKeybind().setCallback(hotkeyCallback);
        return config;
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
}
