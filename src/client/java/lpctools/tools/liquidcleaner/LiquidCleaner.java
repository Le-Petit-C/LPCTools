package lpctools.tools.liquidcleaner;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.HotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.IntegerConfig;
import lpctools.lpcfymasaapi.configbutton.OptionListConfig;
import lpctools.lpcfymasaapi.configbutton.ThirdListConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class LiquidCleaner {
    public static void init(ThirdListConfig LCConfig){
        hotkeyConfig = LCConfig.addHotkeyConfig("LC_Hotkey", "", new HotkeyCallback());
        limitCleaningRange = LCConfig.addThirdListConfig("LC_LimitCleaningRange", false);
        minXConfig = limitCleaningRange.addIntegerConfig("LC_minX", 0);
        maxXConfig = limitCleaningRange.addIntegerConfig("LC_maxX", 0);
        minYConfig = limitCleaningRange.addIntegerConfig("LC_minY", 0);
        maxYConfig = limitCleaningRange.addIntegerConfig("LC_maxY", 0);
        minZConfig = limitCleaningRange.addIntegerConfig("LC_minZ", 0);
        maxZConfig = limitCleaningRange.addIntegerConfig("LC_maxZ", 0);
        valueChangeConfig = limitCleaningRange.addOptionListConfig("LC_ValueChange");
    }
    public static boolean isEnabled(){return onEndTick != null;}
    public static void enable(){
        if(isEnabled()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        player.sendMessage(Text.literal(StringUtils.translate("lpctools.tools.liquidCleaner.enableNotification")), true);
        onEndTick = new OnEndTick();
        Registry.registerEndClientTickCallback(onEndTick);
    }
    public static void disable(@Nullable String reasonKey){
        if(!isEnabled()) return;
        Registry.unregisterEndClientTickCallback(onEndTick);
        onEndTick = null;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        String reason = StringUtils.translate("lpctools.tools.liquidCleaner.disableNotification");
        if(reasonKey != null) reason += " : " + StringUtils.translate(reasonKey);
        player.sendMessage(Text.literal(reason), true);
    }

    static HotkeyConfig hotkeyConfig;
    static ThirdListConfig limitCleaningRange;
    static IntegerConfig minXConfig;
    static IntegerConfig maxXConfig;
    static IntegerConfig minYConfig;
    static IntegerConfig maxYConfig;
    static IntegerConfig minZConfig;
    static IntegerConfig maxZConfig;
    static OptionListConfig<Object> valueChangeConfig;
    @Nullable static OnEndTick onEndTick;

    private static class HotkeyCallback implements IHotkeyCallback{
        @Override public boolean onKeyAction(KeyAction action, IKeybind key) {
            if(isEnabled()) disable(null);
            else enable();
            return true;
        }
    }
}
