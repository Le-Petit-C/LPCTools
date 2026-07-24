package lpctools.mixin.client.events;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Shadow private ClientLevel level;
	@ModifyArg(method = "handleLevelChunkWithLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;queueLightUpdate(Ljava/lang/Runnable;)V"))
	Runnable inject(Runnable updater, @Local(argsOnly = true, ordinal = 0) ClientboundLevelChunkWithLightPacket packet){
		int i = packet.getX();
		int j = packet.getZ();
		return ()->{
			updater.run();
			LevelChunk worldChunk = level.getChunkSource().getChunk(i, j, false);
			if (worldChunk != null)
				Registries.CLIENT_CHUNK_LIGHT_LOAD.runner().onClientWorldChunkLightUpdated(level, worldChunk);
		};
	}
	@WrapOperation(method = "handleMerchantOffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/MerchantMenu;setCanRestock(Z)V"))
	void onMerchantOffersUpdated(MerchantMenu instance, boolean canRestock, Operation<Void> original, @Local(name = "merchantMenu") MerchantMenu merchantMenu) {
		original.call(instance, canRestock);
		Registries.CLIENT_MERCHANT_OFFERS_UPDATED.runner().onMerchantOffersUpdated(merchantMenu);
	}
}
