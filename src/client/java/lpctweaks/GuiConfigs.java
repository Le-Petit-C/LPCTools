package lpctweaks;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class GuiConfigs extends GuiConfigsBase {
    private static ConfigGuiTab tab = ConfigGuiTab.HOTKEYS;
    boolean initialized = false;
    public GuiConfigs() {
        super(10, 50, Reference.MOD_ID, null, "advancements.adventure.avoid_vibration.title", String.format("%s", Reference.MOD_VERSION));
    }

    @Override
    public void initGui()
    {
        //if(initialized) return;
        super.initGui();
        //this.clearOptions();

        int x = 10;
        int y = 26;

        for (ConfigGuiTab tab : ConfigGuiTab.values())
        {
            x += this.createButton(x, y, tab);
        }
        initialized = true;
    }

    private int createButton(int x, int y, ConfigGuiTab tab)
    {
        ButtonGeneric button = new ButtonGeneric(x, y, tab.getName().length() * 10, 20, tab.getName());
        button.setEnabled(GuiConfigs.tab != tab);
        this.addButton(button, new ButtonListener(tab, this));
        return button.getWidth() + 2;
    }

    @Override
    protected boolean useKeybindSearch()
    {
        return GuiConfigs.tab == ConfigGuiTab.HOTKEYS;
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs()
    {
        List<? extends IConfigBase> configs;
        ConfigGuiTab tab = GuiConfigs.tab;

        if (tab == ConfigGuiTab.HOTKEYS) {
            configs = Configs.HOTKEYS_OPTIONS;
        }
        else {
            return Collections.emptyList();
        }

        return ConfigOptionWrapper.createFor(configs);
    }

    private static class ButtonListener implements IButtonActionListener
    {
        private final GuiConfigs parent;
        private final ConfigGuiTab tab;

        public ButtonListener(ConfigGuiTab tab, GuiConfigs parent)
        {
            this.tab = tab;
            this.parent = parent;
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton)
        {
            GuiConfigs.tab = this.tab;
            this.parent.reCreateListWidget(); // apply the new config width
            Objects.requireNonNull(this.parent.getListWidget()).getScrollbar().setValue(0);
            this.parent.initGui();
        }
    }

    private enum ConfigGuiTab{
        HOTKEYS ("HOTKEYS"),
        SECOND_LIST ("SECOND_LIST");
        final String name;
        ConfigGuiTab(String name){
            this.name = name;
        }
        public String getName(){
            return name;
        }
    }
}
