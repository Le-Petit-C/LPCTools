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
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

//TODO:打开高级快捷键设置再关闭后三级列表会出问题

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
    public <T extends LPCConfigList> T addList(T list){
        lists.add(list);
        widgetPosition.add(0);
        return list;
    }
    @SuppressWarnings("unused")
    public LPCConfigList addList(String nameKey){
        return addList(new LPCConfigList(this, nameKey));
    }
    @NotNull public InputHandler getInputHandler(){
        if(inputHandler == null) inputHandler = new InputHandler(modReference);
        return inputHandler;
    }
    //显示当前页面
    public void showPage(){
        if(pageInstance != null) pageInstance.initGui();
        else pageInstance = new ConfigPageInstance();
        if(MinecraftClient.getInstance().currentScreen != pageInstance)
            GuiBase.openGui(pageInstance);
    }
    //如果当前页面正在展示中，刷新当前页面
    public void updateIfCurrent(){
        if(pageInstance != null && MinecraftClient.getInstance().currentScreen == pageInstance)
            pageInstance.initGui();
    }
    //获取当前列
    public LPCConfigList getList(){return lists.get(selectedIndex);}
    @Override public GuiBase get() {
        if(pageInstance == null) pageInstance = new ConfigPageInstance();
        return pageInstance;
    }
    //保存和加载已有的全部配置文件内容
    //如果文件中有目前未注册的配置项，不理它但是保留
    @Override public void load() {
        Path configFile = FileUtils.getConfigDirectoryAsPath().resolve(configFileName);
        if (Files.exists(configFile) && Files.isReadable(configFile)
            && JsonUtils.parseJsonFileAsPath(configFile) instanceof JsonElement pageJson)
            setValueFromJsonElement(pageJson);
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
        for(Map.Entry<String, JsonElement> pair : getAsJsonElement().entrySet())
            pageJson.add(pair.getKey(), pair.getValue());
        Path dir = FileUtils.getConfigDirectoryAsPath();
        if (!Files.exists(dir))
            FileUtils.createDirectoriesIfMissing(dir);
        if (Files.isDirectory(dir)) {
            Path file = dir.resolve(configFileName);
            JsonUtils.writeJsonToFileAsPath(pageJson, file);
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
    private @Nullable ConfigPageInstance pageInstance;
    private void afterInit(){
        ConfigManager.getInstance().registerConfigHandler(modReference.modId, this);
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(modReference.modId, modReference.modName, this));
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

    private class ConfigPageInstance extends GuiConfigsBase{
        @Override public void initGui() {
            super.initGui();
            this.clearOptions();

            int x = 10;
            int y = 26;

            for (int a = 0; a < lists.size(); ++a) {
                String listName = lists.get(a).getTitleDisplayName();
                ButtonGeneric button = new ButtonGeneric(x, y, calculateDisplayLength(listName) * 7, 20, listName);
                button.setEnabled(selectedIndex != a);
                this.addButton(button, new ButtonListener(a, this));
                x += button.getWidth() + 2;
            }
        }
        @Override public List<ConfigOptionWrapper> getConfigs() {
            return lists.get(selectedIndex).buildConfigWrappers(new ArrayList<>());
        }
        @Override public void removed(){
            super.removed();
            pageInstance = null;
            //malilib中并不总会更新热键，比如如果退出配置界面时有配置被ThirdListConfig收起了就不会更新，这样子能强制它更新一下
            InputEventHandler.getKeybindManager().updateUsedKeys();
        }

        @Override protected boolean useKeybindSearch() {return lists.get(selectedIndex).hasHotkeyConfig();}

        ConfigPageInstance() {
            super(10, 50, modReference.modId, null, getFullTranslationKey() + ".title", modReference.getModVersion());
        }
        void select(int index){
            if(index == selectedIndex) return;
            WidgetListConfigOptions widget = getListWidget();
            if(widget != null){
                widgetPosition.set(selectedIndex, widget.getScrollbar().getValue());
                reCreateListWidget(); // apply the new config width
            }
            selectedIndex = index;
            initGui();
            if(widget != null) widget.getScrollbar().setValue(widgetPosition.get(index));
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

        private record ButtonListener(int index, ConfigPageInstance parent) implements IButtonActionListener {
            @Override public void actionPerformedWithButton(ButtonBase button, int mouseButton) {parent.select(index);}
        }
    }
}
