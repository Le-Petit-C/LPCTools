package lpctools.lpcfymasaapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

//单个总设置页面，就是在设置右上角分列出的不同页面
public class LPCConfigPage implements IConfigHandler, Supplier<GuiBase>{
    //构造函数
    public LPCConfigPage(Reference modReference) {
        this.modReference = modReference;
        configFileName = modReference.modId + "-LPCConfig.json";
        if(LPCAPIInit.MASAInitialized) afterInit();
        else {
            uninitializedConfigPages.add(this);
        }
    }
    public Reference getModReference(){return modReference;}
    //给当前页面新添一列
    public LPCConfigList addList(String translationKey){
        lists.add(new LPCConfigList(this, translationKey));
        widgetPosition.add(0);
        return lists.getLast();
    }
    @NotNull public InputHandler getInputHandler(){
        if(inputHandler == null) inputHandler = new InputHandler(modReference);
        return inputHandler;
    }
    //显示当前页面
    public void showPage(){
        if(pageInstance != null) pageInstance.initGui();
        else GuiBase.openGui(pageInstance = new ConfigPageInstance(this));
    }
    //获取当前列
    public LPCConfigList getList(){return lists.get(selectedIndex);}
    @Override public GuiBase get() {return new ConfigPageInstance(this);}
    //保存和加载已有的全部配置文件内容
    //如果文件中有目前未注册的配置项，不理它但是保留
    @Override public void load() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(configFileName);
        if (Files.exists(configFile) && Files.isReadable(configFile)) {
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
            if (element != null && element.isJsonObject()){
                JsonObject pageJson = element.getAsJsonObject();
                for(LPCConfigList list : lists)
                    list.loadConfigListFromJson(pageJson);
            }
            else LPCAPIInit.LOGGER.error(
                    "load(): Failed to parse config file '{}' as a JSON element.",
                    configFile.toAbsolutePath());
        }
    }
    @Override public void save() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(configFileName);
        JsonObject pageJson = null;
        if (Files.exists(configFile) && Files.isReadable(configFile)){
            JsonElement element = JsonUtils.parseJsonFileAsPath(configFile);
            if(element != null && element.isJsonObject())
                pageJson = element.getAsJsonObject();
        }
        if(pageJson == null) pageJson = new JsonObject();
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir))
            FileUtils.createDirectoriesIfMissing(dir);
        if (Files.isDirectory(dir)) {
            for(LPCConfigList list : lists)
                list.addConfigListIntoJson(pageJson);
            Path file = dir.resolve(configFileName);
            JsonUtils.writeJsonToFileAsPath(pageJson, file);
        }
    }

    static void staticAfterInit(){
        if(uninitializedConfigPages == null) return;
        for(LPCConfigPage page : uninitializedConfigPages)
            page.afterInit();
        uninitializedConfigPages = null;
    }

    private static ArrayList<LPCConfigPage> uninitializedConfigPages = new ArrayList<>();
    private InputHandler inputHandler;
    private final Reference modReference;
    private final String configFileName;
    private final @NotNull ArrayList<LPCConfigList> lists = new ArrayList<>();
    private final @NotNull ArrayList<Integer> widgetPosition = new ArrayList<>();
    private int selectedIndex = 0;
    private ConfigPageInstance pageInstance;
    private void afterInit(){
        ConfigManager.getInstance().registerConfigHandler(modReference.modId, this);
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(modReference.modId, modReference.modName, this));
        if(inputHandler == null) inputHandler = new InputHandler(modReference);
    }

    private static class ConfigPageInstance extends GuiConfigsBase{
        @Override public void initGui() {
            super.initGui();
            this.clearOptions();

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
        @Override public List<ConfigOptionWrapper> getConfigs() {
            return parent.lists.get(parent.selectedIndex).buildConfigWrappers(new ArrayList<>());
        }
        @Override public void removed(){
            super.removed();
            parent.lists.get(parent.selectedIndex).callRefresh();
            parent.pageInstance = null;
            //malilib中并不总会更新热键，比如如果退出配置界面时有配置被ThirdListConfig收起了就不会更新，这样子能强制它更新一下
            InputEventHandler.getKeybindManager().updateUsedKeys();
        }

        @Override protected boolean useKeybindSearch() {return parent.lists.get(parent.selectedIndex).hasHotkeyConfig();}

        ConfigPageInstance(LPCConfigPage parent) {
            super(10, 50, parent.modReference.modId, null, parent.modReference.modId + ".configs.title", parent.modReference.getModVersion());
            this.parent = parent;
        }
        void select(int index){
            if(index == parent.selectedIndex) return;
            parent.lists.get(parent.selectedIndex).callRefresh();
            WidgetListConfigOptions widget = getListWidget();
            if(widget != null){
                parent.widgetPosition.set(parent.selectedIndex, widget.getScrollbar().getValue());
                parent.selectedIndex = index;
                reCreateListWidget(); // apply the new config width
                widget.getScrollbar().setValue(parent.widgetPosition.get(index));
            }
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
        private final LPCConfigPage parent;

        private record ButtonListener(int index, ConfigPageInstance parent) implements IButtonActionListener {
            @Override
                    public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
                        parent.select(index);
                    }
                }
    }
}
