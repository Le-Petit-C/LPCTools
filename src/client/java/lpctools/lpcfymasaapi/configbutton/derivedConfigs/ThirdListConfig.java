package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.lpcfymasaapi.LPCConfigPage;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configbutton.ILPCConfig;
import lpctools.lpcfymasaapi.configbutton.ILPCConfigList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

//第三级列表，配置中切换true或false可以展开或收起内含的配置项
public class ThirdListConfig extends BooleanConfig implements ILPCConfigList {
    @NotNull public final ArrayList<ILPCConfig> thirdList = new ArrayList<>();
    public ThirdListConfig(ILPCConfigList defaultParent, String nameKey, boolean defaultBoolean) {
        super(defaultParent, nameKey, defaultBoolean);
        lastValue = defaultBoolean;
        if(parent != null) parent.addConfig(this);
        setCallback(()->{
            if (lastValue != getAsBoolean()){
                //if(GuiUtils.isInTextOrGui())
                getPage().showPage();
                lastValue = getAsBoolean();
            }
        });
    }
    @Override public <T extends ILPCConfig> T addConfig(T config){
        thirdList.add(config);
        if(config instanceof ThirdListConfig thirdListConfig)
            thirdListConfig.parent = this;
        return config;
    }
    @Override public String getFullTranslationKey() {
        return getDefaultParent().getFullTranslationKey() + "." + getNameKey();
    }
    @Override public @NotNull LPCConfigPage getPage() {
        return getDefaultParent().getPage();
    }
    @Override public @NotNull Iterable<ILPCConfig> getConfigs() {
        return thirdList;
    }
    @Override public void callRefresh(){
        super.callRefresh();
        ILPCConfigList.super.callRefresh();
    }
    @Override public void addIntoConfigListJson(@NotNull JsonObject configListJson){
        JsonObject object = new JsonObject();
        object.add("value", getAsJsonElement());
        addConfigListIntoJson(object, "properties");
        configListJson.add(getNameKey(), object);
    }
    @Override public void loadFromConfigListJson(@NotNull JsonObject configListJson){
        if (!configListJson.has(getNameKey())) return;
        JsonElement jsonElement = configListJson.get(getNameKey());
        if(jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.has("value"))
                IGetConfig().setValueFromJsonElement(jsonObject.get("value"));
            loadConfigListFromJson(jsonObject, "properties");
            callRefresh();
        }
    }
    private boolean lastValue;
    @Nullable private ThirdListConfig parent = null;
}
