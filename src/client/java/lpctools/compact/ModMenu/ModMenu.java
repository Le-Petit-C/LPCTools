package lpctools.compact.ModMenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fi.dy.masa.malilib.gui.GuiBase;
import lpctools.LPCTools;

public class ModMenu implements ModMenuApi
{
    @Override public ConfigScreenFactory<?> getModConfigScreenFactory()
    {
        return (screen) -> {
            GuiBase gui = LPCTools.page.get();
            gui.setParent(screen);
            return gui;
        };
    }
}
