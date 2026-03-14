package lpctools.mixin.client.tweaks.blockReplaceHotkey;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lpctools.tweaks.BlockReplaceHotkey;
import net.minecraft.item.BlockItem;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@ModifyExpressionValue(method = "writeNbtToBlockEntity", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z", opcode = Opcodes.GETFIELD))
	private static boolean modifyIsClientTest(boolean original) {
		if(!original) return false;
		else return !BlockReplaceHotkey.shouldModifyClientTest();
	}
}
