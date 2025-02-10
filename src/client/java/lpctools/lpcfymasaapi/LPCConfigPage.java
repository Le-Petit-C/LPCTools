package lpctools.lpcfymasaapi;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

//单个总设置页面，就是在设置右上角分列出的不同页面
public class LPCConfigPage implements IConfigHandler, Supplier<GuiBase>{
    //public methods
    //Constructor 构造函数
    public LPCConfigPage(Reference modReference) {
        this.modReference = modReference;
        if(LPCAPIInit.MASAInitialized) afterInit();
        else {
            uninitializedConfigPages.add(this);
        }
    }

    public Reference getModReference(){
        return modReference;
    }

    public LPCConfigList addList(String translationKey){
        if(lists == null) lists = new ArrayList<>();
        lists.add(new LPCConfigList(this, modReference.modId + ".configs." + translationKey + ".title"));
        return lists.getLast();
    }

    public InputHandler getInputHandler(){
        return inputHandler;
    }

    public Screen newPage(){
        return new ConfigPageInstance(this);
    }

    @Override
    public GuiBase get() {
        return new ConfigPageInstance(this);
    }

    //private static data
    private static ArrayList<LPCConfigPage> uninitializedConfigPages = new ArrayList<>();

    //private nonstatic data
    private InputHandler inputHandler;
    private final Reference modReference;
    private ConfigPageInstance instance;
    private ArrayList<LPCConfigList> lists = null;
    private int selectedIndex = 0;

    //private methods
    private void afterInit(){
        if(instance != null) return;
        instance = new ConfigPageInstance(this);
        ConfigManager.getInstance().registerConfigHandler(modReference.modId, this);
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(modReference.modId, modReference.modName, this));
        inputHandler = new InputHandler(modReference);
        for(LPCConfigList list : lists)
            list.afterInit();
    }

    //public static method but only been called inside APIs
    public static void staticAfterInit(){
        if(uninitializedConfigPages == null) return;
        for(LPCConfigPage page : uninitializedConfigPages){
            page.afterInit();
        }
        uninitializedConfigPages = null;
    }

    //private classes
    private static class ConfigPageInstance extends GuiConfigsBase{
        //private data
        private final LPCConfigPage parent;

        //public methods
        public ConfigPageInstance(LPCConfigPage parent) {
            super(10, 50, parent.modReference.modId, null, parent.modReference.modId + ".configs.title", parent.modReference.modVersion);
            this.parent = parent;
        }

        public void select(int index){
            parent.selectedIndex = index;
            reCreateListWidget(); // apply the new config width
            Objects.requireNonNull(getListWidget()).getScrollbar().setValue(0);
            //TODO:保存滚动条位置
            initGui();
        }

        @Override
        public void initGui() {
            super.initGui();
            this.clearOptions();
            if(parent.lists == null) return;

            int x = 10;
            int y = 26;

            for (int a = 0; a < parent.lists.size(); ++a) {
                String listName = parent.lists.get(a).getDisplayName();
                ButtonGeneric button = new ButtonGeneric(x, y, listName.length() * 10, 20, listName);
                button.setEnabled(parent.selectedIndex != a);
                this.addButton(button, new ButtonListener(a, this));
                x += button.getWidth() + 2;
            }
        }

        @Override
        protected boolean useKeybindSearch() {
            return false;//TODO:检测当前表中有无热键设置再决定是否启用热键查找
            //return LPCConfigPage.tab == ConfigGuiTab.GENERIC;
        }

        @Override
        public List<ConfigOptionWrapper> getConfigs() {
        /*List<? extends IConfigBase> configs;
        ConfigGuiTab tab = LPCConfigPage.tab;
        if (tab == ConfigGuiTab.GENERIC) {
            configs = genericOptions.MASAConfigs;
        }
        else {
            return Collections.emptyList();
        }*/

            return ConfigOptionWrapper.createFor(parent.lists.get(parent.selectedIndex).configs);
        }

        private static class ButtonListener implements IButtonActionListener {
            int index;
            ConfigPageInstance parent;
            ButtonListener(int index, ConfigPageInstance parent){
                this.index = index;
                this.parent = parent;
            }
            @Override
            public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
                parent.select(index);
            }
        }
    }


    @Override
    public void load() {
        //TODO:保存和加载配置文件
    }

    @Override
    public void save() {

    }
}
