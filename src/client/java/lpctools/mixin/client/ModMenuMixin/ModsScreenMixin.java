package lpctools.mixin.client.ModMenuMixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.tweaks.TweakConfigs.modMenuPlayClickSound;

@Pseudo @Mixin(value = ModsScreen.class, remap = false)
public class ModsScreenMixin {
	@Inject(method = "safelyOpenConfigScreen", at = @At("HEAD"), remap = false)
	void injectSafelyOpenConfigScreenHead(String modId, CallbackInfo ci){
		if(modMenuPlayClickSound.getAsBoolean())
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
