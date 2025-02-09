package lpctweaks.configbutton;

import fi.dy.masa.malilib.gui.GuiConfigsBase;
import lpctweaks.Reference;

import java.util.List;

public class LPCGuiConfigs extends GuiConfigsBase{
    public LPCGuiConfigs() {
        super(10, 50, Reference.MOD_ID, null, "lpctweaks.gui.title.configs", String.format("%s", Reference.MOD_VERSION));
    }

    @Override
    public List<GuiConfigsBase.ConfigOptionWrapper> getConfigs() {
        return List.of();
    }

    @Override
    public void initGui()
    {
        super.initGui();
        this.clearOptions();
    }
}
