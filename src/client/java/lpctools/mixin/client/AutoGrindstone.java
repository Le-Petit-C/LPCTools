package lpctools.mixin.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

@Mixin(Minecraft.class)
public class AutoGrindstone {
    @Inject(method = "setScreenAndShow", at = @At("RETURN"))
    void mixinScreenRender(Screen screen, CallbackInfo ci){
        if(!(screen instanceof GrindstoneScreen)) return;
        if(!lpctools.tools.autoGrindstone.AutoGrindstone.AGConfig.getBooleanValue()) return;
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        MultiPlayerGameMode itm = client.gameMode;
        if(player == null || itm == null) {
            lpctools.tools.autoGrindstone.AutoGrindstone.AGConfig.setBooleanValue(false);
            return;
        }
        Inventory inventory = player.getInventory();
        Object2IntOpenHashMap<String> enchantmentIds = new Object2IntOpenHashMap<>();
        for(String key : lpctools.tools.autoGrindstone.AutoGrindstone.limitEnchantmentsConfig){
            String[] splits = key.split(";");
            if(splits.length >= 3){
                warnInvalidEnchantment(key, player);
                continue;
            }
            if(splits.length == 2){
                try{
                    int maxLevel = Integer.parseInt(splits[1].trim());
                    enchantmentIds.addTo(splits[0].trim(), maxLevel);
                }catch (NumberFormatException ignored){
                    warnInvalidEnchantment(key, player);
                }
            }
            else enchantmentIds.addTo(splits[0].trim(), Integer.MAX_VALUE);
        }
        List<ItemStack> mainStacks = inventory.getNonEquipmentItems();
        for(int n = 0; n < mainStacks.size(); ++n){
            ItemStack stack = mainStacks.get(n);
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
            if(enchantments.isEmpty()) continue;
            boolean canErase = true;
            for(Object2IntMap.Entry<Holder<Enchantment>> enchantment : enchantments.entrySet()){
                String enchantmentId = enchantment.getKey().getRegisteredName();
                int enchantmentLevelLimit;
                if(enchantmentIds.containsKey(enchantmentId))
                    enchantmentLevelLimit = enchantmentIds.getInt(enchantmentId);
                else {
                    int lastIndex = enchantmentId.lastIndexOf(':');
                    if (lastIndex == -1) return;
                    String enchantmentIdTail = enchantmentId.substring(lastIndex + 1);
                    if(!enchantmentIds.containsKey(enchantmentIdTail)){
                        canErase = false;
                        break;
                    }
                    enchantmentLevelLimit = enchantmentIds.getInt(enchantmentIdTail);
                }
                if(enchantmentLevelLimit < enchantment.getIntValue()){
                    canErase = false;
                    break;
                }
            }
            if(canErase){
                int slot = n < 9 ? n + 30 : n - 6;
                itm.handleContainerInput(player.containerMenu.containerId, slot, 0, ContainerInput.QUICK_MOVE, player);
                itm.handleContainerInput(player.containerMenu.containerId, 2, 0, ContainerInput.THROW, player);
            }
        }
        client.setScreenAndShow(null);
    }
    @Unique private static void warnInvalidEnchantment(String key, LocalPlayer player){
        player.sendSystemMessage(Component.nullToEmpty(String.format("§eInvalid enchantment string: %s", key)));
    }
}
