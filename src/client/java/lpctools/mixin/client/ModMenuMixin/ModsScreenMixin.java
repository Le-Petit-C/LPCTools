package lpctools.mixin.client.ModMenuMixin;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
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
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}
}
