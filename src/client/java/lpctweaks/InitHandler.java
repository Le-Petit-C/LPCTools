package lpctweaks;

import fi.dy.masa.malilib.MaLiLibInputHandler;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.IHotkeyCallback;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class InitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(Reference.MOD_ID, new Configs());
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(Reference.MOD_ID, Reference.MOD_NAME, GuiConfigs::new));
        InputEventHandler.getKeybindManager().registerKeybindProvider(InputHandler.getInstance());
        Configs.OpenGuiConfigs.getKeybind().setCallback(new MyHotkeyCallback());
    }

    private static class MyHotkeyCallback implements IHotkeyCallback
    {
        @Override
        public boolean onKeyAction(KeyAction action, IKeybind key)
        {
            //if (action == KeyAction.PRESS)
            {
                // 在这里处理热键按下事件
                MinecraftClient.getInstance().player.sendMessage(Text.of("Key press detected!"), true);
                //System.out.println("Hotkey pressed!");
            }
            return true;
        }
    }
}
