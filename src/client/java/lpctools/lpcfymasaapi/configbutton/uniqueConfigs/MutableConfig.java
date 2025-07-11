package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.*;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.interfaces.*;
import lpctools.util.DataUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;
import static lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.*;

public class MutableConfig extends ButtonThirdListConfig implements IThirdListBase, IMutableConfig {
    boolean condenseOperationButton;
    boolean hideOperationButton;
    @Override public boolean doCondenseOperationButton() {
        if(getParent() instanceof IMutableConfig config)
            return config.doCondenseOperationButton();
        else return condenseOperationButton;
    }
    @Override public boolean doHideOperationButton() {
        if(getParent() instanceof IMutableConfig config)
            return config.doHideOperationButton();
        else return hideOperationButton;
    }
    
    public record ConfigAllocator<T extends ILPCUniqueConfigBase>(
        String nameKey, BiFunction<MutableConfig, String, T> allocator) {}
    private final LinkedHashMap<String, ConfigAllocator<?>> allocatorMap = new LinkedHashMap<>();
    public MutableConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey,
                         @NotNull ImmutableList<ConfigAllocator<?>> configSuppliers, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, null);
        setListener((button, mouseButton)->onAddConfigClicked(getConfigs().size()));
        buttonName = titleKey;
        setValueChangeCallback(callback);
        for(ConfigAllocator<?> allocator : configSuppliers) allocatorMap.put(allocator.nameKey, allocator);
    }
    
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
        super.getButtonOptions(res);
        if(getParent() instanceof IMutableConfig) return;
        res.add(new ButtonOption(-1, (button, mouseButton)->{
            hideOperationButton = !hideOperationButton;
            getPage().updateIfCurrent();},
            ()->hideOperationButton ? "<" : ">",
            buttonGenericAllocator));
        if(!hideOperationButton)
            res.add(new ButtonOption(-1, (button, mouseButton)->{
                condenseOperationButton = !condenseOperationButton;
                getPage().updateIfCurrent();},
                ()->condenseOperationButton ? "<>" : "><",
                buttonGenericAllocator));
    }
    
    @Override public @Nullable JsonObject getAsJsonElement() {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();
        LPCConfigList list = new LPCConfigList(getParent(), getNameKey());
        for(ILPCConfig config : getConfigs()){
            if(config instanceof MutableConfigOption<?> mutableConfigOption)
                array.add(mutableConfigOption.getAsJsonElement());
            else {
                array.add(new JsonPrimitive(config.getNameKey()));
                list.addConfig(config);
            }
        }
        object.add("mutableValues", array);
        object.add(propertiesId, list.getAsJsonElement());
        object.addProperty("extended", extended);
        if(!(getParent() instanceof MutableConfig)){
            object.addProperty("condense", condenseOperationButton);
            object.addProperty("hide", hideOperationButton);
        }
        return object;
    }
    
    @Override public void setValueFromJsonElement(@NotNull JsonElement data) {
        if( !(data instanceof JsonObject object)){
            warnFailedLoadingConfig(this, data);
            return;
        }
        HashMap<String, ILPCConfig> configs = new HashMap<>();
        getConfigs().forEach(config->{if(!(config instanceof MutableConfigOption)) configs.put(config.getNameKey(), config);});
        getConfigs().clear();
        getConfigs().addAll(configs.values());
        if(object.get(propertiesId) instanceof JsonElement properties)
            subConfigs.setValueFromJsonElement(properties);
        getConfigs().clear();
        if(object.get("mutableValues") instanceof JsonArray mutableValues)
            for(JsonElement element : mutableValues)
                if(loadFromJsonElement(element, configs) instanceof ILPCConfig config)
                    addConfig(config);
        for(ILPCConfig config : configs.values())
            addConfig(config);
        onValueChanged();
        if(object.get("extended") instanceof JsonPrimitive primitive)
            extended = primitive.getAsBoolean();
        if(!(getParent() instanceof MutableConfig)){
            if(object.get("condense") instanceof JsonPrimitive primitive)
                condenseOperationButton = primitive.getAsBoolean();
            if(object.get("hide") instanceof JsonPrimitive primitive)
                hideOperationButton = primitive.getAsBoolean();
        }
    }
    
    private @Nullable ILPCConfig loadFromJsonElement(@NotNull JsonElement data, HashMap<String, ILPCConfig> staticConfigs){
        Supplier<MutableConfigOption<?>> fail = ()->{
            warnFailedLoadingConfig(this, data);
            return null;
        };
        if(data instanceof JsonObject object){
            if(!(object.get("supplier") instanceof JsonPrimitive primitive)) return fail.get();
            MutableConfigOption<?> config = allocateConfig(primitive.getAsString());
            if(config == null) return fail.get();
            if(object.get("value") instanceof JsonElement element)
                config.wrappedConfig.setValueFromJsonElement(element);
            return config;
        }
        else{
            if (!(data instanceof JsonPrimitive primitive)) return fail.get();
            return staticConfigs.remove(primitive.getAsString());
        }
    }
    
    @Override public @NotNull StringBuilder getFullPath() {
        ILPCConfigList parent = getParent();
        if(parent instanceof MutableConfig) return getParent().getFullPath();
        else return super.getFullPath();
    }
    
    public class MutableConfigOption<T extends ILPCUniqueConfigBase> extends LPCConfigBase{
        public final T wrappedConfig;
        public boolean flipPosButton, flipMinusButton;
        private MutableConfigOption(@NotNull T wrappedConfig) {
            super(MutableConfig.this, "", null);
            this.wrappedConfig = wrappedConfig;
        }
        @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
            wrappedConfig.getButtonOptions(res);
            if(doHideOperationButton()) return;
            if(doCondenseOperationButton()){
                res.add(new ButtonOption(-1, this::posListener, null, iconButtonAllocator(flipPosButton ? MaLiLibIcons.ARROW_DOWN : MaLiLibIcons.ARROW_UP, LeftRight.CENTER)));
                res.add(new ButtonOption(-1, this::minusListener, null, iconButtonAllocator(flipMinusButton ? MaLiLibIcons.PLUS : MaLiLibIcons.MINUS, LeftRight.CENTER)));
            }
            else{
                res.add(new ButtonOption(-1, (b, m)->posListener(b, 0), null, getConfigs().getFirst() != this ? iconButtonAllocator(MaLiLibIcons.ARROW_UP, LeftRight.CENTER) : null));
                res.add(new ButtonOption(-1, (b, m)->posListener(b, 1), null, getConfigs().getLast() != this ? iconButtonAllocator(MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER) : null));
                res.add(new ButtonOption(-1, (b, m)->minusListener(b, 1), null, iconButtonAllocator(MaLiLibIcons.PLUS, LeftRight.CENTER)));
                res.add(new ButtonOption(-1, (b, m)->minusListener(b, 0), null, iconButtonAllocator(MaLiLibIcons.MINUS, LeftRight.CENTER)));
            }
        }
        private int getPosition(){
            int a;
            ArrayList<ILPCConfig> configs = getConfigs();
            for(a = 0; a < configs.size(); ++a)
                if(configs.get(a) == this) break;
            return a;
        }
        private void posListener(ButtonBase ignored, int mouseButton){
            ArrayList<ILPCConfig> configs = getConfigs();
            int position = getPosition();
            if(doCondenseOperationButton() && flipPosButton && (mouseButton == 0 || mouseButton == 1))
                mouseButton = 1 - mouseButton;
            if(mouseButton == 0) {//往上移动
                if(position - 1 < 0) return;
                Collections.swap(configs, position, position - 1);
            }
            else if(mouseButton == 1) {//往下移动
                if(position + 1 >= configs.size()) return;
                Collections.swap(configs, position, position + 1);
            }
            else if(mouseButton == 2) flipPosButton = !flipPosButton;
            getPage().updateIfCurrent();
        }
        private void minusListener(ButtonBase ignored, int mouseButton){
            ArrayList<ILPCConfig> configs = getConfigs();
            int position = getPosition();
            if(doCondenseOperationButton() && flipPosButton && (mouseButton == 0 || mouseButton == 1))
                mouseButton = 1 - mouseButton;
            if(mouseButton == 0) configs.remove(position);//删除
            else if(mouseButton == 1) onAddConfigClicked(position);//添加
            else if(mouseButton == 2) flipMinusButton = !flipMinusButton;
            getPage().updateIfCurrent();
        }
        @Override public @Nullable JsonElement getAsJsonElement() {
            JsonObject object = new JsonObject();
            object.addProperty("supplier", wrappedConfig.getNameKey());
            object.add("value", wrappedConfig.getAsJsonElement());
            return object;
        }
        @Deprecated @Override public void setValueFromJsonElement(@NotNull JsonElement data) {
            throw new UnsupportedOperationException();
        }
        @Override public @NotNull String getFullTranslationKey() {
            if(wrappedConfig == null) return super.getFullTranslationKey();
            else return wrappedConfig.getFullTranslationKey();
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            MutableConfig.this.onValueChanged();
        }
    }
    public class ThirdListMutableConfigOption<T extends ILPCUniqueConfigBase & IThirdListBase> extends MutableConfigOption<T> implements ILPCConfigList{
        private ThirdListMutableConfigOption(@NotNull T wrappedConfig) {
            super(wrappedConfig);
        }
        @Override public @NotNull Collection<ILPCConfig> getConfigs() {
            return wrappedConfig.getConfigs();
        }
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            return wrappedConfig.buildConfigWrappers(wrapperList);
        }
    }
    @Nullable MutableConfig.MutableConfigOption<?> allocateConfig(String supplierId){
        ConfigAllocator<?> allocator = allocatorMap.get(supplierId);
        if(allocator == null) return null;
        ILPCUniqueConfigBase config = allocator.allocator.apply(this, allocator.nameKey);
        if(config instanceof IThirdListBase)
            return new ThirdListMutableConfigOption<>((IThirdListBase & ILPCUniqueConfigBase)config);
        else return new MutableConfigOption<>(config);
    }
    public class AddConfigScreen extends GuiBase {
        public final int position;
        AddConfigScreen(int position){
            this.position = position;
            setParent(mc.currentScreen);
            setTitle(Text.translatable(titleKey).getString());
        }
        void onButtonClicked(String id){
            MutableConfigOption<?> config = allocateConfig(id);
            closeGui(true);
            if(config == null) return;
            extended = true;
            getConfigs().add(position, config);
            getPage().updateIfCurrent();
            onValueChanged();
        }
        @Override public void initGui() {
            super.initGui();
            int dy = 22;
            int x = getScreenWidth() / 2;
            int y = (getScreenHeight() - dy * allocatorMap.size()) / 2;
            for(ConfigAllocator<?> allocator : allocatorMap.values()){
                String text = ((ILPCConfigKeyProvider) () -> DataUtils.appendNodeIfNotEmpty(MutableConfig.this.getFullPath(), allocator.nameKey).toString()).getTitleTranslation();
                addButton(allocateCenterAt(x, y, text), (button, mouse)->onButtonClicked(allocator.nameKey));
                y += dy;
            }
            addButton(allocateCenterAt(x, y, Text.translatable(cancelKey).getString()), (button, mouse)->onButtonClicked(null));
        }
        
        private static ButtonGeneric allocateCenterAt(int centerX, int centerY, String text){
            int w = calculateAndAdjustDisplayLength(text);
            return new ButtonGeneric(centerX - w / 2, centerY - 10, w, 20, text);
        }
        
        @Override public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTicks) {
            if (this.getParent() != null)
                this.getParent().render(drawContext, mouseX, mouseY, partialTicks);
            super.render(drawContext, mouseX, mouseY, partialTicks);
        }
    }
    public void onAddConfigClicked(int position){
        MinecraftClient mc = MinecraftClient.getInstance();
        AddConfigScreen screen = new AddConfigScreen(position);
        mc.currentScreen = null;
        mc.setScreen(screen);
    }
    private static final String titleKey = "lpcfymasaapi.configs.mutableConfig.title";
    private static final String cancelKey = "lpcfymasaapi.configs.mutableConfig.cancel";
}
