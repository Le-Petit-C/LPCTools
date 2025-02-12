package lpctools.tools.fillingassistant;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FillingAssistant {
    @NotNull public static final ImmutableList<Item> defaultPlaceableItemList = ImmutableList.of(Items.STONE);
    @NotNull public static final ImmutableList<String> defaultPlaceableItemIdList = getDefaultPlaceableItemIdList();
    @Nullable public static HashSet<String> getPlaceableItemIds(){return placeableItemIds;}
    public static void rebuildPlaceableItems(List<String> idList){placeableItemIds = new HashSet<>(idList);}
    @NotNull public static IHotkeyCallback getHotkeyCallback(){return new HotkeyCallback();}
    @NotNull public static IValueChangeCallback<ConfigStringList> getPlaceableItemsChangeCallback() {return new PlaceableItemsChangeCallback();}
    @NotNull public static Runnable getThreadCallback(){return new ThreadCallback();}

    @NotNull
    private static ImmutableList<String> getDefaultPlaceableItemIdList(){
        ArrayList<String> ret = new ArrayList<>();
        for(Item item : defaultPlaceableItemList)
            ret.add(item.toString());
        return ImmutableList.copyOf(ret);
    }
    private static class HotkeyCallback implements IHotkeyCallback{
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key) {
            Data.switchPlaceMode();
            return true;
        }
    }
    @Nullable private static HashSet<String> placeableItemIds = new HashSet<>(getDefaultPlaceableItemIdList());
    private static class PlaceableItemsChangeCallback implements IValueChangeCallback<ConfigStringList>{
        @Override
        public void onValueChanged(ConfigStringList config) {
            rebuildPlaceableItems(config.getStrings());
        }
    }
    private static class ThreadCallback implements Runnable{
        @Override
        public void run(){
            @NotNull PlaceBlockTick placer = new PlaceBlockTick();
            while(Data.enabled()){
                placer.tick(MinecraftClient.getInstance());
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {}
            }
        }
    }
}
