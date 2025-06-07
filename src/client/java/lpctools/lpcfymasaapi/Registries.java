package lpctools.lpcfymasaapi;

import net.minecraft.client.gui.screen.Screen;

public class Registries {
    public static final UnregistrableRegistry<ScreenChangeCallback> ON_SCREEN_CHANGED = new UnregistrableRegistry<>(
        callbacks->newScreen->callbacks.forEach(screen->screen.onScreenChanged(newScreen)));
    
    public interface ScreenChangeCallback{
        void onScreenChanged(Screen newScreen);
    }
}
