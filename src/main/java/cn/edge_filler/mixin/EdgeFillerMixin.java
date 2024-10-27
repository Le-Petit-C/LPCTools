package cn.edge_filler.mixin;

import cn.edge_filler.EdgeFillerMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class EdgeFillerMixin {
	@Inject(at = @At("HEAD"), method = "loadWorld")
	private void init(CallbackInfo info) {
		EdgeFillerMod.LOGGER.info("log1");
		// This code is injected into the start of MinecraftServer.loadWorld()V
	}
}