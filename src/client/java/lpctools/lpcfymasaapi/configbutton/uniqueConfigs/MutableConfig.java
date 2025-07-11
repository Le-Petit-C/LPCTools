package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.*;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.interfaces.*;
import lpctools.util.DataUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;
import static lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.*;

public class MutableConfig extends ButtonThirdListConfig implements IThirdListBase, IMutableConfig, IConfigResettable {
    private boolean condenseOperationButton;
    private boolean hideOperationButton;
    private final JsonObject defaultJson;
    
    //getConfigs()获取到的是包装后的配置，如果要遍历被包装的配置，应该调用这个
    public Iterable<ILPCUniqueConfigBase> iterateConfigs(){
        return new Iterable<>() {
            @Override public @NotNull Iterator<ILPCUniqueConfigBase> iterator() {
                return new Iterator<>() {
                    private final Iterator<ILPCConfig> iterator = getConfigs().iterator();
                    ILPCUniqueConfigBase next = null;
                    @Override public boolean hasNext() {
                        while(next == null){
                            if(!iterator.hasNext()) break;
                            if(iterator.next() instanceof MutableConfigOption<?> config)
                                next = config.wrappedConfig;
                        }
                        return next != null;
                    }
                    @Override public ILPCUniqueConfigBase next() {
                        ILPCUniqueConfigBase next = this.next;
                        this.next = null;
                        return next;
                    }
                };
            }
        };
    }
    
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
    @Override public boolean isModified() {return !defaultJson.equals(getAsJsonElement());}
    @Override public void resetToDefault() {
        setValueFromJsonElement(defaultJson);
        getPage().updateIfCurrent();
    }
    
    public record ConfigAllocator<T extends ILPCUniqueConfigBase, U>(
        String nameKey, U userData, TriFunction<MutableConfig, String, @Nullable U, T> allocator) {
        public ConfigAllocator(String nameKey, BiFunction<MutableConfig, String, T> allocator){
            this(nameKey, null, (parent, key, user)->allocator.apply(parent, key));
        }
    }
    private final LinkedHashMap<String, ConfigAllocator<?, ?>> allocatorMap = new LinkedHashMap<>();
    public <U> MutableConfig(@NotNull ILPCConfigList parent, @NotNull String nameKey,
                         @NotNull ImmutableList<ConfigAllocator<?, U>> configSuppliers,
                        @Nullable ImmutableMap<String, U> defaultValues,
                         @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, null);
        setListener((button, mouseButton)->onAddConfigClicked(getConfigs().size()));
        buttonName = titleKey;
        setValueChangeCallback(callback);
        for(ConfigAllocator<?, U> allocator : configSuppliers) allocatorMap.put(allocator.nameKey, allocator);
        if(defaultValues != null){
            HashMap<String, ConfigAllocator<?, U>> map = new HashMap<>();
            for(ConfigAllocator<?, U> allocator : configSuppliers) map.put(allocator.nameKey, allocator);
            defaultValues.forEach((key, userData)->{
                ConfigAllocator<?, U> allocator = map.get(key);
                if(allocator != null) addConfig(wrapConfig(allocator.allocator.apply(this, key, userData)));
            });
        }
        defaultJson = getAsJsonElement();
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
    
    @Override public @NotNull JsonObject getAsJsonElement() {
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
        if(object.get("extended") instanceof JsonPrimitive primitive)
            extended = primitive.getAsBoolean();
        if(!(getParent() instanceof MutableConfig)){
            if(object.get("condense") instanceof JsonPrimitive primitive)
                condenseOperationButton = primitive.getAsBoolean();
            if(object.get("hide") instanceof JsonPrimitive primitive)
                hideOperationButton = primitive.getAsBoolean();
        }
        onValueChanged();
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
    
    public class MutableConfigOption<T extends ILPCUniqueConfigBase> extends LPCUniqueConfigBase {
        public final T wrappedConfig;
        public boolean flipPosButton, flipMinusButton;
        private MutableConfigOption(@NotNull T wrappedConfig) {
            super(MutableConfig.this, "", MutableConfig.this::onValueChanged);
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
    private @NotNull MutableConfig.MutableConfigOption<?> wrapConfig(@NotNull ILPCUniqueConfigBase config){
        if(config instanceof IThirdListBase)
            return new ThirdListMutableConfigOption<>((IThirdListBase & ILPCUniqueConfigBase)config);
        else return new MutableConfigOption<>(config);
    }
    private @Nullable MutableConfig.MutableConfigOption<?> allocateConfig(String supplierId){
        ConfigAllocator<?, ?> allocator = allocatorMap.get(supplierId);
        if(allocator == null) return null;
        return wrapConfig(allocator.allocator.apply(this, allocator.nameKey, null));
    }
    private void allocateAndAddConfig(String id, int position){
        MutableConfigOption<?> config = allocateConfig(id);
        if(config == null) return;
        extended = true;
        getConfigs().add(position, config);
        getPage().updateIfCurrent();
        onValueChanged();
    }
    public class AddConfigScreen extends GuiBase {
        public final int position;
        AddConfigScreen(int position){
            this.position = position;
            setParent(mc.currentScreen);
            setTitle(Text.translatable(titleKey).getString());
        }
        void onButtonClicked(String id){
            allocateAndAddConfig(id, position);
            closeGui(true);
        }
        @Override public void initGui() {
            super.initGui();
            int dy = 22;
            int x = getScreenWidth() / 2;
            int y = (getScreenHeight() - dy * allocatorMap.size()) / 2;
            for(ConfigAllocator<?, ?> allocator : allocatorMap.values()){
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
        if(allocatorMap.size() == 1){
            allocateAndAddConfig(allocatorMap.keySet().iterator().next(), position);
            return;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        AddConfigScreen screen = new AddConfigScreen(position);
        mc.currentScreen = null;
        mc.setScreen(screen);
    }
    private static final String titleKey = "lpcfymasaapi.configs.mutableConfig.title";
    private static final String cancelKey = "lpcfymasaapi.configs.mutableConfig.cancel";
}
