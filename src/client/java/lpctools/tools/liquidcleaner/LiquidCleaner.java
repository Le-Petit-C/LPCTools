package lpctools.tools.liquidcleaner;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.*;
import lpctools.tools.ToolConfigs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class LiquidCleaner {
    public static void init(ThirdListConfig LCConfig){
        hotkeyConfig = LCConfig.addHotkeyConfig("LC_Hotkey", "", new HotkeyCallback());
        limitInteractSpeedConfig = LCConfig.addThirdListConfig("LC_limitInteractSpeed", false);
        maxBlockPerTickConfig = limitInteractSpeedConfig.addDoubleConfig("LC_maxBlockPerTick", 1.0, 0, 64);
        reachDistanceConfig = LCConfig.addDoubleConfig("LC_reachDistance", 4.5, 0, 5);
        disableOnGUIOpened = LCConfig.addBooleanConfig("FA_disableOnGUIOpened", false);
        limitCleaningRange = LCConfig.addThirdListConfig("LC_LimitCleaningRange", false);
        minXConfig = limitCleaningRange.addIntegerConfig("LC_minX", Integer.MIN_VALUE);
        maxXConfig = limitCleaningRange.addIntegerConfig("LC_maxX", Integer.MAX_VALUE);
        minYConfig = limitCleaningRange.addIntegerConfig("LC_minY", Integer.MIN_VALUE);
        maxYConfig = limitCleaningRange.addIntegerConfig("LC_maxY", Integer.MAX_VALUE);
        minZConfig = limitCleaningRange.addIntegerConfig("LC_minZ", Integer.MIN_VALUE);
        maxZConfig = limitCleaningRange.addIntegerConfig("LC_maxZ", Integer.MAX_VALUE);
        valueChangeConfig = limitCleaningRange.addOptionListConfig("LC_ValueChange");
        valueChangeConfig.addOption("minX", minXConfig);
        valueChangeConfig.addOption("maxX", maxXConfig);
        valueChangeConfig.addOption("minY", minYConfig);
        valueChangeConfig.addOption("maxY", maxYConfig);
        valueChangeConfig.addOption("minZ", minZConfig);
        valueChangeConfig.addOption("maxZ", maxZConfig);
        valueAddHotkeyConfig = limitCleaningRange.addHotkeyConfig("LC_AddValueKey", "",
                new HotkeyConfig.IntegerChanger(1, valueChangeConfig, limitCleaningRange));
        valueSubtractHotkeyConfig = limitCleaningRange.addHotkeyConfig("LC_SubtractValueKey", "",
                new HotkeyConfig.IntegerChanger(-1, valueChangeConfig, limitCleaningRange));
    }
    public static boolean isEnabled(){return onEndTick != null;}
    public static void enableTool(){
        if(isEnabled()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;
        player.sendMessage(Text.literal(StringUtils.translate("lpctools.tools.LC_enableNotification")), true);
        onEndTick = new OnEndTick();
        Registry.registerEndClientTickCallback(onEndTick);
    }
    public static void disableTool(@Nullable String reasonKey){
        if(!isEnabled()) return;
        Registry.unregisterEndClientTickCallback(onEndTick);
        onEndTick = null;
        ToolConfigs.displayDisableReason("LC_disableNotification", reasonKey);
    }

    static HotkeyConfig hotkeyConfig;
    static ThirdListConfig limitInteractSpeedConfig;
    static DoubleConfig maxBlockPerTickConfig;
    static DoubleConfig reachDistanceConfig;
    static BooleanConfig disableOnGUIOpened;
    static ThirdListConfig limitCleaningRange;
    static IntegerConfig minXConfig;
    static IntegerConfig maxXConfig;
    static IntegerConfig minYConfig;
    static IntegerConfig maxYConfig;
    static IntegerConfig minZConfig;
    static IntegerConfig maxZConfig;
    static OptionListConfig<IntegerConfig> valueChangeConfig;
    static HotkeyConfig valueAddHotkeyConfig;
    static HotkeyConfig valueSubtractHotkeyConfig;
    @Nullable static OnEndTick onEndTick;

    private static class HotkeyCallback implements IHotkeyCallback{
        @Override public boolean onKeyAction(KeyAction action, IKeybind key) {
            if(isEnabled()) disableTool(null);
            else enableTool();
            return true;
        }
    }
}
