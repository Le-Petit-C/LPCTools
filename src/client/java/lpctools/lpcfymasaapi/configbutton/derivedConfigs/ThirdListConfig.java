package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configbutton.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfig;
import lpctools.lpcfymasaapi.implementations.ILPCConfigList;
import lpctools.lpcfymasaapi.implementations.IThirdListBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

//第三级列表，配置中切换true或false可以展开或收起内含的配置项
public class ThirdListConfig extends BooleanConfig implements IThirdListBase {
    public ThirdListConfig(ILPCConfigList parent, String nameKey, boolean defaultBoolean) {
        super(parent, nameKey, defaultBoolean);
        lastValue = defaultBoolean;
        setValueChangeCallback(()->{
            if (lastValue != getAsBoolean()){
                //if(GuiUtils.isInTextOrGui())
                getPage().showPage();
                lastValue = getAsBoolean();
            }
        });
        subConfigs = new LPCConfigList(parent, nameKey);
    }

    @Override public @NotNull Collection<ILPCConfig> getConfigs() {return subConfigs.getConfigs();}
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(getAsBoolean()) return subConfigs.buildConfigWrappers(wrapperList);
        else return wrapperList;
    }

    @Override public void addIntoConfigListJson(@NotNull JsonObject configListJson){
        JsonObject object = new JsonObject();
        object.add("value", getAsJsonElement());
        addConfigListIntoJson(object, "properties");
        configListJson.add(this.getName(), object);
    }
    @Override public void loadFromConfigListJson(@NotNull JsonObject configListJson){
        if (!configListJson.has(this.getName())) return;
        JsonElement jsonElement = configListJson.get(this.getName());
        if(jsonElement.isJsonObject()){
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.has("value"))
                setValueFromJsonElement(jsonObject.get("value"));
            loadConfigListFromJson(jsonObject, "properties");
        }
    }
    private boolean lastValue;
    private final LPCConfigList subConfigs;
}
