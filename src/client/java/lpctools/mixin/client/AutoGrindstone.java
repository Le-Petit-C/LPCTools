package lpctools.mixin.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GrindstoneScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GrindstoneScreen.class)
public class AutoGrindstone {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    void mixinScreenRender(CallbackInfo ci){
        if(!lpctools.tools.autoGrindstone.AutoGrindstone.autoGrindstoneConfig.getAsBoolean()) return;
        ci.cancel();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager itm = client.interactionManager;
        if(player == null || itm == null) return;
        PlayerInventory inventory = player.getInventory();
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
        List<ItemStack> mainStacks = inventory.main;
        for(int n = 0; n < mainStacks.size(); ++n){
            ItemStack stack = mainStacks.get(n);
            ItemEnchantmentsComponent enchantments = EnchantmentHelper.getEnchantments(stack);
            if(enchantments.isEmpty()) continue;
            boolean canErase = true;
            for(Object2IntMap.Entry<RegistryEntry<Enchantment>> enchantment : enchantments.getEnchantmentEntries()){
                String enchantmentId = enchantment.getKey().getIdAsString();
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
                itm.clickSlot(player.currentScreenHandler.syncId, slot, 0, SlotActionType.QUICK_MOVE, player);
                itm.clickSlot(player.currentScreenHandler.syncId, 2, 0, SlotActionType.THROW, player);
            }
        }
        client.setScreen(null);
    }
    @Unique private static void warnInvalidEnchantment(String key, ClientPlayerEntity player){
        player.sendMessage(Text.of(String.format("§eInvalid enchantment string: %s", key)), false);
    }
}
