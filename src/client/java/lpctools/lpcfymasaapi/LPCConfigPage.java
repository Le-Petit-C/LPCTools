package lpctools.lpcfymasaapi;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptions;
//import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
//import fi.dy.masa.malilib.util.data.ModInfo;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

//单个总设置页面，就是在设置右上角分列出的不同页面
public class LPCConfigPage implements IConfigHandler, Supplier<GuiBase>, ILPCConfigBase {
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
    public LPCConfigList addList(String nameKey){
        lists.add(new LPCConfigList(this, nameKey));
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
        else pageInstance = new ConfigPageInstance(this);
        if(MinecraftClient.getInstance().currentScreen != pageInstance)
            GuiBase.openGui(pageInstance);
    }
    //获取当前列
    public LPCConfigList getList(){return lists.get(selectedIndex);}
    @Override public GuiBase get() {
        if(pageInstance == null) pageInstance = new ConfigPageInstance(this);
        return pageInstance;
    }
    //保存和加载已有的全部配置文件内容
    //如果文件中有目前未注册的配置项，不理它但是保留
    @Override public void load() {
        File configFile = new File(FileUtils.getConfigDirectory(), configFileName);
        if (configFile.exists() && configFile.isFile() && configFile.canRead()
            && JsonUtils.parseJsonFile(configFile) instanceof JsonElement pageJson)
            setValueFromJsonElement(pageJson);
    }
    @Override public void save() {
        File configFile = new File(FileUtils.getConfigDirectory(), configFileName);
        JsonObject pageJson = null;
        if (configFile.exists() && configFile.isFile() && configFile.canRead()){
            JsonElement element = JsonUtils.parseJsonFile(configFile);
            if(element != null && element.isJsonObject())
                pageJson = element.getAsJsonObject();
        }
        if(pageJson == null) pageJson = new JsonObject();
        for(Map.Entry<String, JsonElement> pair : getAsJsonElement().entrySet())
            pageJson.add(pair.getKey(), pair.getValue());
        File dir = FileUtils.getConfigDirectory();
        if (!dir.exists() && !dir.mkdir()) return;
        if (dir.isDirectory()) {
            File file = new File(dir, configFileName);
            JsonUtils.writeJsonToFile(pageJson, file);
        }
    }
    @Override public @NotNull JsonObject getAsJsonElement() {
        JsonObject pageJson = new JsonObject();
        for(LPCConfigList list : lists)
            list.addIntoParentJsonObject(pageJson);
        return pageJson;
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement data) {
        if(data instanceof JsonObject jsonObject){
            for(LPCConfigList list : lists)
                list.setValueFromParentJsonObject(jsonObject);
        }
        else warnFailedLoadingConfig(this, data);
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
        //Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(modReference.modId, modReference.modName, this));
        if(inputHandler == null) inputHandler = new InputHandler(modReference);
    }

    @Override public @NotNull ILPCConfigBase getParent() {return this;}
    @Override public @NotNull String getNameKey() {return "configs";}
    @Override public @NotNull String getAlignSpaces(){return "";}
    //只有LPCConfigPage重载此方法，结束递归
    @Override public @NotNull StringBuilder getFullPath() {
        return new StringBuilder(getModReference().modId).append('.').append(getNameKey());
    }
    
    @Override public @NotNull LPCConfigPage getPage() {return this;}

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
            parent.pageInstance = null;
            //malilib中并不总会更新热键，比如如果退出配置界面时有配置被ThirdListConfig收起了就不会更新，这样子能强制它更新一下
            InputEventHandler.getKeybindManager().updateUsedKeys();
        }

        @Override protected boolean useKeybindSearch() {return parent.lists.get(parent.selectedIndex).hasHotkeyConfig();}

        ConfigPageInstance(LPCConfigPage parent) {
            super(10, 50, parent.modReference.modId, null, parent.getFullTranslationKey() + ".title", parent.modReference.getModVersion());
            this.parent = parent;
        }
        void select(int index){
            if(index == parent.selectedIndex) return;
            WidgetListConfigOptions widget = getListWidget();
            if(widget != null){
                parent.widgetPosition.set(parent.selectedIndex, widget.getScrollbar().getValue());
                reCreateListWidget(); // apply the new config width
            }
            parent.selectedIndex = index;
            initGui();
            if(widget != null) widget.getScrollbar().setValue(parent.widgetPosition.get(index));
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
            @Override public void actionPerformedWithButton(ButtonBase button, int mouseButton) {parent.select(index);}
        }
    }
}
