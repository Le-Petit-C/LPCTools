package lpctools.mixin.client.tweaks.blockReplaceHotkey;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lpctools.tweaks.VanillaBlockInteractionModifier;
import net.minecraft.world.item.BlockItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@ModifyExpressionValue(method = "updateCustomBlockEntityTag(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isClientSide()Z"))
	private static boolean modifyIsClientTest(boolean original) {
		if(!original) return false;
		else return !VanillaBlockInteractionModifier.shouldModifyClientTest();
	}
}
