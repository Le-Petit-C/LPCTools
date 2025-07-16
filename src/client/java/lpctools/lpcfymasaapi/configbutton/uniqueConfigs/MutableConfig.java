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
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
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
import static lpctools.util.AlgorithmUtils.*;

public class MutableConfig<T extends ILPCUniqueConfigBase> extends LPCUniqueConfigBase implements ILPCConfigReadable, IMutableConfig, IConfigResettable {
    private boolean condenseOperationButton;
    private boolean hideOperationButton;
    private final JsonObject defaultJson;
    private final ArrayList<MutableConfigOption<? extends T>> subConfigs = new ArrayList<>();
    @Nullable public String buttonName;
    public boolean expanded;
    
    //getConfigs()获取到的是包装后的配置，如果要遍历被包装的配置，应该调用这个
    public Iterable<T> iterateConfigs(){return convertIterable(subConfigs, config->config.wrappedConfig);}
    
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
    
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(expanded) return ILPCConfigReadable.defaultBuildConfigWrappers(wrapperList, subConfigs, true);
        else return wrapperList;
    }
    
    public record ConfigAllocator<T extends ILPCUniqueConfigBase, U>(
        String nameKey, U userData, TriFunction<MutableConfig<T>, String, @Nullable U, ? extends T> allocator) {
        public ConfigAllocator(String nameKey, BiFunction<MutableConfig<T>, String, ? extends T> allocator){
            this(nameKey, null, (parent, key, user)->allocator.apply(parent, key));
        }
    }
    private final LinkedHashMap<String, ConfigAllocator<T, ?>> allocatorMap = new LinkedHashMap<>();
    public <U> MutableConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey,
                             @NotNull ImmutableList<ConfigAllocator<T, U>> configSuppliers,
                             @Nullable ImmutableMap<String, U> defaultValues,
                             @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, null);
        buttonName = titleKey;
        setValueChangeCallback(callback);
        for(ConfigAllocator<T, U> allocator : configSuppliers) allocatorMap.put(allocator.nameKey, allocator);
        if(defaultValues != null){
            HashMap<String, ConfigAllocator<T, U>> map = new HashMap<>();
            for(ConfigAllocator<T, U> allocator : configSuppliers) map.put(allocator.nameKey, allocator);
            defaultValues.forEach((key, userData)->{
                ConfigAllocator<T, U> allocator = map.get(key);
                if(allocator != null) subConfigs.add(wrapConfig(allocator.allocator.apply(this, key, userData)));
            });
        }
        defaultJson = getAsJsonElement();
    }
    
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
        res.add(new ButtonOption(-1, (button, mouseButton)->{expanded = !expanded; getPage().updateIfCurrent();}, null,
            ILPCUniqueConfigBase.iconButtonAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER)));
        res.add(new ButtonOption(1, (button, mouseButton)->onAddConfigClicked(subConfigs.size()), ()->buttonName, buttonGenericAllocator));
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
        for(MutableConfigOption<? extends T> config : subConfigs)
            array.add(config.getAsJsonElement());
        object.add("mutableValues", array);
        object.addProperty("expanded", expanded);
        if(!(getParent() instanceof MutableConfig)){
            object.addProperty("condense", condenseOperationButton);
            object.addProperty("hide", hideOperationButton);
        }
        return object;
    }
    
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
        if(!(data instanceof JsonObject object)){
            warnFailedLoadingConfig(this, data);
            return new UpdateTodo();
        }
        subConfigs.clear();
        if(object.get("mutableValues") instanceof JsonArray mutableValues)
            for(JsonElement element : mutableValues)
                if(loadFromJsonElement(element) instanceof MutableConfigOption<? extends T> config)
                    subConfigs.add(config);
        if(object.get("expanded") instanceof JsonPrimitive primitive)
            expanded = primitive.getAsBoolean();
        if(!(getParent() instanceof MutableConfig)){
            if(object.get("condense") instanceof JsonPrimitive primitive)
                condenseOperationButton = primitive.getAsBoolean();
            if(object.get("hide") instanceof JsonPrimitive primitive)
                hideOperationButton = primitive.getAsBoolean();
        }
        return new UpdateTodo().valueChanged();
    }
    
    private @Nullable MutableConfigOption<? extends T> loadFromJsonElement(@NotNull JsonElement data){
        Supplier<MutableConfigOption<? extends T>> fail = ()->{
            warnFailedLoadingConfig(this, data);
            return null;
        };
        if(data instanceof JsonObject object){
            if(!(object.get("supplier") instanceof JsonPrimitive primitive)) return fail.get();
            MutableConfigOption<? extends T> config = allocateConfig(primitive.getAsString());
            if(config == null) return fail.get();
            if(object.get("value") instanceof JsonElement element)
                config.wrappedConfig.setValueFromJsonElement(element);
            return config;
        }
        else return fail.get();
    }
    
    @Override public @NotNull StringBuilder getFullPath() {
        ILPCConfigReadable parent = getParent();
        if(parent instanceof MutableConfig) return getParent().getFullPath();
        else return super.getFullPath();
    }
    
    public static class MutableConfigOption<T extends ILPCUniqueConfigBase> extends LPCUniqueConfigBase {
        public final T wrappedConfig;
        public boolean flipPosButton, flipMinusButton;
        public final MutableConfig<? super T> parent;
        private MutableConfigOption(MutableConfig<? super T> parent, @NotNull T wrappedConfig) {
            super(parent, "", parent::onValueChanged);
            this.wrappedConfig = wrappedConfig;
            this.parent = parent;
        }
        @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
            wrappedConfig.getButtonOptions(res);
            if(parent.doHideOperationButton()) return;
            if(parent.doCondenseOperationButton()){
                res.add(new ButtonOption(-1, this::posListener, null, iconButtonAllocator(flipPosButton ? MaLiLibIcons.ARROW_DOWN : MaLiLibIcons.ARROW_UP, LeftRight.CENTER)));
                res.add(new ButtonOption(-1, this::minusListener, null, iconButtonAllocator(flipMinusButton ? MaLiLibIcons.PLUS : MaLiLibIcons.MINUS, LeftRight.CENTER)));
            }
            else{
                res.add(new ButtonOption(-1, (b, m)->posListener(b, 0), null, parent.subConfigs.getFirst() != this ? iconButtonAllocator(MaLiLibIcons.ARROW_UP, LeftRight.CENTER) : null));
                res.add(new ButtonOption(-1, (b, m)->posListener(b, 1), null, parent.subConfigs.getLast() != this ? iconButtonAllocator(MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER) : null));
                res.add(new ButtonOption(-1, (b, m)->minusListener(b, 1), null, iconButtonAllocator(MaLiLibIcons.PLUS, LeftRight.CENTER)));
                res.add(new ButtonOption(-1, (b, m)->minusListener(b, 0), null, iconButtonAllocator(MaLiLibIcons.MINUS, LeftRight.CENTER)));
            }
        }
        private int getPosition(){
            int a;
            for(a = 0; a < parent.subConfigs.size(); ++a)
                if(parent.subConfigs.get(a) == this) break;
            return a;
        }
        private void posListener(ButtonBase ignored, int mouseButton){
            int position = getPosition();
            if(parent.doCondenseOperationButton() && flipPosButton && (mouseButton == 0 || mouseButton == 1))
                mouseButton = 1 - mouseButton;
            if(mouseButton == 0) {//往上移动
                if(position - 1 < 0) return;
                Collections.swap(parent.subConfigs, position, position - 1);
            }
            else if(mouseButton == 1) {//往下移动
                if(position + 1 >= parent.subConfigs.size()) return;
                Collections.swap(parent.subConfigs, position, position + 1);
            }
            else if(mouseButton == 2) flipPosButton = !flipPosButton;
            getPage().updateIfCurrent();
        }
        private void minusListener(ButtonBase ignored, int mouseButton){
            int position = getPosition();
            if(parent.doCondenseOperationButton() && flipPosButton && (mouseButton == 0 || mouseButton == 1))
                mouseButton = 1 - mouseButton;
            if(mouseButton == 0) {
                parent.subConfigs.remove(position);//删除
                parent.onValueChanged();
                getPage().updateIfCurrent();
            }
            else if(mouseButton == 1) parent.onAddConfigClicked(position);//添加
            else if(mouseButton == 2) {
                flipMinusButton = !flipMinusButton;
                getPage().updateIfCurrent();
            }
        }
        @Override public @Nullable JsonElement getAsJsonElement() {
            JsonObject object = new JsonObject();
            object.addProperty("supplier", wrappedConfig.getNameKey());
            object.add("value", wrappedConfig.getAsJsonElement());
            return object;
        }
        @Deprecated @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
            throw new UnsupportedOperationException();
        }
        @Override public @NotNull String getFullTranslationKey() {
            if(wrappedConfig == null) return super.getFullTranslationKey();
            else return wrappedConfig.getFullTranslationKey();
        }
    }
    public static class ThirdListMutableConfigOption<T extends ILPCUniqueConfigBase, U extends ILPCConfigReadable> extends MutableConfigOption<T> implements ILPCConfigReadable {
        private final U wrappedConfig2;
        private ThirdListMutableConfigOption(MutableConfig<? super T> parent, @NotNull T wrappedConfig1, @NotNull U wrappedConfig2) {
            super(parent, wrappedConfig1);
            this.wrappedConfig2 = wrappedConfig2;
        }
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            return wrappedConfig2.buildConfigWrappers(wrapperList);
        }
    }
    private @NotNull <U extends T> MutableConfig.MutableConfigOption<U> wrapConfig(@NotNull U config){
        if(config instanceof ILPCConfigReadable displayable)
            return new ThirdListMutableConfigOption<>(this, config, displayable);
        else return new MutableConfigOption<>(this, config);
    }
    private @Nullable MutableConfig.MutableConfigOption<? extends T> allocateConfig(String supplierId){
        ConfigAllocator<T, ?> allocator = allocatorMap.get(supplierId);
        if(allocator == null) return null;
        return wrapConfig(allocator.allocator.apply(this, allocator.nameKey, null));
    }
    private void allocateAndAddConfig(String id, int position){
        MutableConfigOption<? extends T> config = allocateConfig(id);
        if(config == null) return;
        expanded = true;
        subConfigs.add(position, config);
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
        mc.setScreen(new AddConfigScreen(position));
    }
    private static final String titleKey = "lpcfymasaapi.configs.mutableConfig.title";
    private static final String cancelKey = "lpcfymasaapi.configs.mutableConfig.cancel";
}
