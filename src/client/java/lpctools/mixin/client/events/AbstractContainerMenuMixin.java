package lpctools.mixin.client.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.lpcfymasaapi.Registries.CLIENT_CONTAINER_CONTENT_INITIALIZED;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin {
	@Inject(method = "initializeContents", at = @At("TAIL"))
	public void injectInitializeContentsTail(CallbackInfo ci) {
		if(RenderSystem.isOnRenderThread()) CLIENT_CONTAINER_CONTENT_INITIALIZED.runner().onContainerContentInitialized((AbstractContainerMenu)(Object)this);
	}
}
