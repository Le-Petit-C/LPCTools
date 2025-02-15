package lpctools.lpcfymasaapi;

import com.google.common.collect.ImmutableList;
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
import lpctools.lpcfymasaapi.configbutton.LPCConfig;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
        if(lists == null) lists = new ArrayList<>();
        lists.add(new LPCConfigList(this, translationKey));
        return lists.getLast();
    }
    public InputHandler getInputHandler(){return inputHandler;}
    //显示当前页面
    public void showPage(){GuiBase.openGui(new ConfigPageInstance(this));}
    public void setCallback(@Nullable IConfigPageCallback callback){this.callback = callback;}
    @Nullable public IConfigPageCallback getCallback(){return callback;}
    @Override public GuiBase get() {return new ConfigPageInstance(this);}
    //保存和加载已有的全部配置文件内容
    //如果文件中有目前未注册的配置项，不理它但是保留
    @Override public void load() {
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
        if(callback != null)
            callback.onPageRefresh();
        for (LPCConfigList list : lists) {
            IConfigListCallback listCallback = list.getCallback();
            if (listCallback != null)
                listCallback.onListRefresh();
        }
    }
    @Override public void save() {
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

    static void staticAfterInit(){
        if(uninitializedConfigPages == null) return;
        for(LPCConfigPage page : uninitializedConfigPages){
            page.afterInit();
        }
        uninitializedConfigPages = null;
    }

    private static ArrayList<LPCConfigPage> uninitializedConfigPages = new ArrayList<>();
    private InputHandler inputHandler;
    private final Reference modReference;
    private final String configFileName;
    private ArrayList<LPCConfigList> lists = null;
    private int selectedIndex = 0;
    private JsonObject configPageJson;
    @Nullable private IConfigPageCallback callback = null;
    private void afterInit(){
        ConfigManager.getInstance().registerConfigHandler(modReference.modId, this);
        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(modReference.modId, modReference.modName, this));
        inputHandler = new InputHandler(modReference);
        load();
    }
    private void resetConfigPageJson(JsonObject configPageJson){
        this.configPageJson = configPageJson;
        if(this.configPageJson == null)
            this.configPageJson = new JsonObject();
        for(LPCConfigList list : lists)
            list.resetListJson(this.configPageJson);
    }

    private static class ConfigPageInstance extends GuiConfigsBase{
        @Override public void initGui() {
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
        @Override public List<ConfigOptionWrapper> getConfigs() {
            ImmutableList.Builder<ConfigOptionWrapper> builder = ImmutableList.builder();
            for (LPCConfig config : parent.lists.get(parent.selectedIndex).configs) {
                if(config.enabled)
                    builder.add(new ConfigOptionWrapper(config.getConfig()));
            }
            return builder.build();
        }
        @Override public void removed(){
            super.removed();
            if(parent.callback != null)
                parent.callback.onPageRefresh();
            IConfigListCallback listCallback = parent.lists.get(parent.selectedIndex).getCallback();
            if(listCallback != null)
                listCallback.onListRefresh();
        }

        @Override protected boolean useKeybindSearch() {return parent.lists.get(parent.selectedIndex).hasHotkeyConfig();}

        ConfigPageInstance(LPCConfigPage parent) {
            super(10, 50, parent.modReference.modId, null, parent.modReference.modId + ".configs.title", parent.modReference.getModVersion());
            this.parent = parent;
        }
        void select(int index){
            if(index == parent.selectedIndex) return;
            IConfigListCallback listCallback = parent.lists.get(parent.selectedIndex).getCallback();
            if(listCallback != null)
                listCallback.onListRefresh();
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
        private final LPCConfigPage parent;

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
}
