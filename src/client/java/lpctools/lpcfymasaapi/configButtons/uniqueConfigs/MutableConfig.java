package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.*;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import lpctools.util.DataUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;
import static lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.*;
import static lpctools.util.AlgorithmUtils.*;

public class MutableConfig<T extends ILPCUniqueConfigBase> extends LPCUniqueConfigBase implements ILPCConfigReadable, IMutableConfig, IConfigResettable, AutoCloseable {
    public final @NotNull ImmutableMap<String, ? extends BiFunction<MutableConfig<T>, String, T>> configSuppliers;
    private boolean condenseOperationButton;
    private boolean hideOperationButton;
    private JsonArray defaultJson;
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
    @Override public boolean isModified() {return !defaultJson.equals(getSubConfigsAsJsonElement());}
    @Override public void resetToDefault() {
        setSubConfigsValueFromJsonElement(defaultJson);
        getPage().updateIfCurrent();
    }
    
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(expanded) return ILPCConfigReadable.defaultBuildConfigWrappers(wrapperList, subConfigs, true);
        else return wrapperList;
    }
    
    @Override public void close() {
        for(MutableConfigOption<? extends T> config : subConfigs)
            config.close();
        subConfigs.clear();
    }
    public MutableConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey,
                             @NotNull ImmutableMap<String, BiFunction<MutableConfig<T>, String, T>> configSuppliers,
                             @Nullable ILPCValueChangeCallback callback){
        super(parent, nameKey, null);
        this.configSuppliers = configSuppliers;
        buttonName = titleKey;
        setValueChangeCallback(callback);
        defaultJson = getSubConfigsAsJsonElement();
    }
    public <U> MutableConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey,
                         @NotNull ImmutableMap<String, TriFunction<MutableConfig<T>, String, U, T>> configSuppliers,
                         @Nullable ILPCValueChangeCallback callback, U userData) {
        this(parent, nameKey, convertSuppliers(configSuppliers, userData), callback);
    }
    private static <T extends ILPCUniqueConfigBase, U> ImmutableMap<String, BiFunction<MutableConfig<T>, String, T>>
    convertSuppliers(Map<String, TriFunction<MutableConfig<T>, String, U, T>> configSuppliers, U userData){
        LinkedHashMap<String, BiFunction<MutableConfig<T>, String, T>> convertedSuppliers = new LinkedHashMap<>();
        configSuppliers.forEach((k, v)->convertedSuppliers.put(k, (c, s)->v.apply(c, s, userData)));
        return ImmutableSortedMap.copyOf(convertedSuppliers);
    }
    public void setCurrentAsDefault(boolean expanded){
        defaultJson = getSubConfigsAsJsonElement();
        this.expanded = expanded;
    }
    
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
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
    
    JsonArray getSubConfigsAsJsonElement(){
        JsonArray array = new JsonArray();
        for(MutableConfigOption<? extends T> config : subConfigs)
            array.add(config.getAsJsonElement());
        return array;
    }
    
    @Override public @NotNull JsonObject getAsJsonElement() {
        JsonObject object = new JsonObject();
        object.add("mutableValues", getSubConfigsAsJsonElement());
        object.addProperty("expanded", expanded);
        if(!(getParent() instanceof MutableConfig)){
            object.addProperty("condense", condenseOperationButton);
            object.addProperty("hide", hideOperationButton);
        }
        return object;
    }
    public void setSubConfigsValueFromJsonElement(@NotNull JsonElement data){
        for(MutableConfigOption<? extends T> config : subConfigs)
            config.close();
        subConfigs.clear();
        if(data instanceof JsonArray mutableValues){
            for(JsonElement element : mutableValues){
                MutableConfigOption<? extends T> config = loadFromJsonElement(element);
                if(config != null) subConfigs.add(config);
            }
        }
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
        if(!(data instanceof JsonObject object)){
            warnFailedLoadingConfig(this, data);
            return new UpdateTodo();
        }
        setSubConfigsValueFromJsonElement(object.get("mutableValues"));
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
    
    public static class MutableConfigOption<T extends ILPCUniqueConfigBase> extends LPCUniqueConfigBase implements AutoCloseable{
        public final T wrappedConfig;
        public boolean flipPosButton, flipMinusButton;
        public final MutableConfig<? super T> parent;
        private MutableConfigOption(MutableConfig<? super T> parent, @NotNull T wrappedConfig) {
            super(parent, "", parent::onValueChanged);
            this.wrappedConfig = wrappedConfig;
            this.parent = parent;
        }
        @Override public void getButtonOptions(ButtonOptionArrayList res) {
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
                parent.subConfigs.remove(position).close();//删除
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
        @Override public void close() {
            if(wrappedConfig instanceof AutoCloseable closeable){
                try {
                    closeable.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
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
    private @NotNull <V extends T> MutableConfig.MutableConfigOption<V> wrapConfig(@NotNull V config){
        if(config instanceof ILPCConfigReadable displayable)
            return new ThirdListMutableConfigOption<>(this, config, displayable);
        else return new MutableConfigOption<>(this, config);
    }
    private @Nullable MutableConfig.MutableConfigOption<? extends T> allocateConfig(String supplierId){
        BiFunction<MutableConfig<T>, String, T> allocator = configSuppliers.get(supplierId);
        if(allocator == null) return null;
        return wrapConfig(allocator.apply(this, supplierId));
    }
    private T allocateAndAddConfig(String id, int position){
        MutableConfigOption<? extends T> config = allocateConfig(id);
        if(config == null) return null;
        expanded = true;
        subConfigs.add(position, config);
        getPage().updateIfCurrent();
        onValueChanged();
        return config.wrappedConfig;
    }
    public T allocateAndAddConfig(String id){return allocateAndAddConfig(id, subConfigs.size());}
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
            MutableInt y = new MutableInt((getScreenHeight() - dy * configSuppliers.size()) / 2);
            configSuppliers.forEach((key, value)->{
                String text = ((ILPCConfigKeyProvider) () -> DataUtils.appendNodeIfNotEmpty(MutableConfig.this.getFullPath(), key).toString()).getTitleTranslation();
                addButton(allocateCenterAt(x, y.getAndAdd(dy), text), (button, mouse)->onButtonClicked(key));
            });
            addButton(allocateCenterAt(x, y.intValue(), Text.translatable(cancelKey).getString()), (button, mouse)->onButtonClicked(null));
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
        if(configSuppliers.size() == 1)
            configSuppliers.forEach((key, value)->allocateAndAddConfig(key, position));
        else{
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.setScreen(new AddConfigScreen(position));
        }
    }
    private static final String titleKey = "lpcfymasaapi.configs.mutableConfig.title";
    private static final String cancelKey = "lpcfymasaapi.configs.mutableConfig.cancel";
}
