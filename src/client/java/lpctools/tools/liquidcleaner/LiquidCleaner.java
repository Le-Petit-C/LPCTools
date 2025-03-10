package lpctools.tools.liquidcleaner;

import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import lpctools.lpcfymasaapi.Registry;
import lpctools.lpcfymasaapi.configbutton.HotkeyConfig;
import lpctools.lpcfymasaapi.configbutton.ThirdListConfig;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class LiquidCleaner {
    //public static Set<Block> liquidBlocks = Set.of(Blocks.WATER, Blocks.LAVA, Blocks.BUBBLE_COLUMN, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS);
    //public static Set<Block> breakableBlocks = Set.of(Blocks.SLIME_BLOCK, Blocks.HONEY_BLOCK, Blocks.KELP, Blocks.KELP_PLANT);
    public static Set<Item> placeableItems = Set.of(Items.SLIME_BLOCK, Items.HONEY_BLOCK);
    public static void init(ThirdListConfig LCConfig){
        hotkeyConfig = LCConfig.addHotkeyConfig("LC_Hotkey", "", new HotkeyCallback());
    }
    public static boolean isEnabled(){return onEndTick != null;}
    public static void enable(){
        if(onEndTick == null) {
            onEndTick = new OnEndTick();
            Registry.registerEndClientTickCallback(onEndTick);
        }
    }
    public static void disable(){
        if(onEndTick != null){
            Registry.unregisterEndClientTickCallback(onEndTick);
            onEndTick = null;
        }
    }

    static HotkeyConfig hotkeyConfig;
    @Nullable static OnEndTick onEndTick;

    private static class HotkeyCallback implements IHotkeyCallback{
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            if(isEnabled()) disable();
            else enable();
            return true;
        }
    }
}
