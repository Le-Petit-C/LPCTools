package lpctools.lpcfymasaapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import net.minecraft.client.gui.screen.Screen;

import java.nio.file.Files;
import java.nio.file.Path;
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
        configFileName = modReference.modId + "-LPCConfig.json";
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
        lists.add(new LPCConfigList(this, translationKey));
        return lists.getLast();
    }

    public InputHandler getInputHandler(){
        return inputHandler;
    }

    public void showPage(){
        GuiBase.openGui(new ConfigPageInstance(this));
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
    private final String configFileName;
    private ArrayList<LPCConfigList> lists = null;
    private int selectedIndex = 0;
    private JsonObject configPageJson;

    //private methods
    private void afterInit(){
        ConfigManager.getInstance().registerConfigHandler(modReference.modId, this);
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(modReference.modId, modReference.modName, this));
        inputHandler = new InputHandler(modReference);
        for(LPCConfigList list : lists)
            list.afterInit();
        load();
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

        private static int calculateDisplayLength(String str) {
            int length = 0;
            for (char c : str.toCharArray()) {
                // 检查字符是否是 ASCII 打印字符（半角字符）
                if (c >= 0x20 && c <= 0x7E) {
                    length += 1;
                } else {
                    length += 2;
                }
            }
            return length;
        }

        @Override
        public void initGui() {
            super.initGui();
            this.clearOptions();
            if(parent.lists == null) return;

            int x = 10;
            int y = 26;

            for (int a = 0; a < parent.lists.size(); ++a) {
                String listName = parent.lists.get(a).getTitleDisplayName();
                ButtonGeneric button = new ButtonGeneric(x, y, calculateDisplayLength(listName) * 7, 20, listName);
                button.setEnabled(parent.selectedIndex != a);
                this.addButton(button, new ButtonListener(a, this));
                x += button.getWidth() + 2;
            }
        }

        @Override
        protected boolean useKeybindSearch() {
            return parent.lists.get(parent.selectedIndex).hasHotkeyConfig();
        }

        @Override
        public List<ConfigOptionWrapper> getConfigs() {
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

    private void resetConfigPageJson(JsonObject configPageJson){
        this.configPageJson = configPageJson;
        if(this.configPageJson == null)
            this.configPageJson = new JsonObject();
        for(LPCConfigList list : lists)
            list.resetListJson(this.configPageJson);
    }

    //保存和加载已有的全部配置文件内容
    //如果文件中有目前未注册的配置项，不理它但是保留
    @Override
    public void load() {
        JsonObject object = null;
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(configFileName);
        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
            if (element != null && element.isJsonObject())
                object = element.getAsJsonObject();
            else LPCAPIInit.LOGGER.error(
                    "load(): Failed to parse config file '{}' as a JSON element.",
                    configFile.toAbsolutePath());
        }
        resetConfigPageJson(object);
    }

    @Override
    public void save() {
        for(LPCConfigList list : lists)
            list.reloadConfigJson();
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir))
            FileUtils.createDirectoriesIfMissing(dir);
        if (Files.isDirectory(dir)) {
            Path file = dir.resolve(configFileName);
            JsonUtils.writeJsonToFileAsPath(configPageJson, file);
        }
    }
}
