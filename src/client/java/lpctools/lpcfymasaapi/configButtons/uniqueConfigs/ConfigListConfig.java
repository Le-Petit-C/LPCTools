package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;
import static lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase.iconButtonAllocator;
import static lpctools.util.AlgorithmUtils.convertIterable;

public class ConfigListConfig<T extends ILPCUniqueConfigBase> extends LPCUniqueConfigBase implements ILPCConfigReadable, IMutableConfig, IConfigResettable{
    public final @NotNull Function<? super ConfigListConfig<T>, ? extends T> configSupplier;
    private boolean condenseOperationButton;
    private boolean hideOperationButton;
    private @NotNull JsonArray defaultJson;
    protected final ArrayList<MutableConfigOption<? extends T>> subConfigs = new ArrayList<>();
    @Nullable public String buttonName;
    public boolean expanded;
    private boolean isModified = false;
    private boolean needUpdateModified = true;
    
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
    @Override public boolean isModified() {
        if(needUpdateModified) {
            needUpdateModified = false;
            isModified = !defaultJson.equals(getSubConfigsAsJsonElement());
        }
        return isModified;
    }
    @Override public void resetToDefault() {
        setSubConfigsValueFromJsonElement(defaultJson);
        getPage().markNeedUpdate();
        onValueChanged();
    }
    @Override public @NotNull Iterable<? extends ILPCConfig> getConfigs(){return subConfigs;}
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(expanded) return ILPCConfigReadable.super.buildConfigWrappers(getStringWidth, wrapperList);
        else return wrapperList;
    }
    int indent;
    @Override public void setAlignedIndent(int indent) {this.indent = indent;}
    @Override public int getAlignedIndent() {return indent;}
    public ConfigListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey,
                         @NotNull Function<? super ConfigListConfig<T>, ? extends T> configSupplier, @Nullable ILPCValueChangeCallback callback){
        super(parent, nameKey, null);
        this.configSupplier = configSupplier;
        buttonName = titleKey;
        setValueChangeCallback(callback);
        defaultJson = getSubConfigsAsJsonElement();
    }
    public void setCurrentAsDefault(boolean expanded){
        defaultJson = getSubConfigsAsJsonElement();
        needUpdateModified = true;
        this.expanded = expanded;
    }
    
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        res.add(new ButtonOption(-1, (button, mouseButton)->{expanded = !expanded; getPage().markNeedUpdate();}, null,
            ILPCUniqueConfigBase.iconButtonAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER)));
        res.add(new ButtonOption(1, (button, mouseButton)->onAddConfigClicked(subConfigs.size()), ()->buttonName, buttonGenericAllocator));
        if(getParent() instanceof IMutableConfig) return;
        res.add(new ButtonOption(-1, (button, mouseButton)->{
            hideOperationButton = !hideOperationButton;
            getPage().markNeedUpdate();},
            ()->hideOperationButton ? "<" : ">",
            buttonGenericAllocator));
        if(!hideOperationButton)
            res.add(new ButtonOption(-1, (button, mouseButton)->{
                condenseOperationButton = !condenseOperationButton;
                getPage().markNeedUpdate();},
                ()->condenseOperationButton ? "<>" : "><",
                buttonGenericAllocator));
    }
    
    JsonArray getSubConfigsAsJsonElement(){
        JsonArray array = new JsonArray();
        for(MutableConfigOption<? extends T> config : subConfigs)
            array.add(config.getAsJsonElement());
        return array;
    }
    
    @Override public @Nullable JsonElement getAsJsonElement() {
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
				subConfigs.add(config);
            }
        }
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
        if(!(data instanceof JsonObject object)){
            warnFailedLoadingConfig(this, data);
            return new UpdateTodo();
        }
        if(object.get("mutableValues") instanceof JsonElement element)
            setSubConfigsValueFromJsonElement(element);
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
    
    private @NotNull MutableConfigOption<? extends T> loadFromJsonElement(@NotNull JsonElement data){
        MutableConfigOption<? extends T> config = allocateConfig();
        config.wrappedConfig.setValueFromJsonElement(data);
        return config;
    }
    
    public static class MutableConfigOption<T extends ILPCUniqueConfigBase> extends LPCUniqueConfigBase implements AutoCloseable{
        public final T wrappedConfig;
        public boolean flipPosButton, flipMinusButton;
        public final ConfigListConfig<? super T> parent;
        private MutableConfigOption(ConfigListConfig<? super T> parent, @NotNull T wrappedConfig) {
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
            getPage().markNeedUpdate();
        }
        private void minusListener(ButtonBase ignored, int mouseButton){
            int position = getPosition();
            if(parent.doCondenseOperationButton() && flipPosButton && (mouseButton == 0 || mouseButton == 1))
                mouseButton = 1 - mouseButton;
            if(mouseButton == 0) {
                parent.subConfigs.remove(position).close();//删除
                parent.onValueChanged();
                getPage().markNeedUpdate();
            }
            else if(mouseButton == 1) parent.onAddConfigClicked(position);//添加
            else if(mouseButton == 2) {
                flipMinusButton = !flipMinusButton;
                getPage().markNeedUpdate();
            }
        }
        @Override public @Nullable JsonElement getAsJsonElement() {
            return wrappedConfig.getAsJsonElement();
        }
        @Deprecated @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
            return wrappedConfig.setValueFromJsonElementEx(data);
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
        private ThirdListMutableConfigOption(ConfigListConfig<? super T> parent, @NotNull T wrappedConfig1, @NotNull U wrappedConfig2) {
            super(parent, wrappedConfig1);
            this.wrappedConfig2 = wrappedConfig2;
        }
        @Override public @NotNull Iterable<? extends ILPCConfig> getConfigs(){return wrappedConfig2.getConfigs();}
        @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
            return wrappedConfig2.buildConfigWrappers(getStringWidth, wrapperList);
        }
        @Override public void setAlignedIndent(int indent) {wrappedConfig2.setAlignedIndent(indent);}
        @Override public int getAlignedIndent() {return wrappedConfig2.getAlignedIndent();}
    }
    private @NotNull <V extends T> MutableConfigOption<V> wrapConfig(@NotNull V config){
        if(config instanceof ILPCConfigReadable displayable)
            return new ThirdListMutableConfigOption<>(this, config, displayable);
        else return new MutableConfigOption<>(this, config);
    }
    private @NotNull MutableConfigOption<? extends T> allocateConfig(){
        return wrapConfig(configSupplier.apply(this));
    }
    private T allocateAndAddConfig(int position){
        MutableConfigOption<? extends T> config = allocateConfig();
		expanded = true;
        subConfigs.add(position, config);
        getPage().markNeedUpdate();
        onValueChanged();
        return config.wrappedConfig;
    }
    public T allocateAndAddConfig(){return allocateAndAddConfig(subConfigs.size());}
    public void onAddConfigClicked(int position){allocateAndAddConfig(position);}
    private static final String titleKey = "lpcfymasaapi.configs.mutableConfig.title";
    @Override public void onValueChanged() {
        needUpdateModified = true;
        super.onValueChanged();
    }
}
