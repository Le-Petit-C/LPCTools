package lpctools.compact.ModMenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fi.dy.masa.malilib.gui.GuiBase;
import lpctools.LPCTools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

import static lpctools.generic.GenericConfigs.playClickSoundFromModMenu;
import static lpctools.tweaks.TweakConfigs.modMenuPlayClickSound;

public class ModMenu implements ModMenuApi {
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (screen) -> {
            GuiBase gui = LPCTools.page.get();
            gui.setParent(screen);
            if(playClickSoundFromModMenu.getAsBoolean() && !modMenuPlayClickSound.getAsBoolean())
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return gui;
        };
    }
}
