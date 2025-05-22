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

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

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

    public static final String superJsonId = "value";
    public static final String propertiesId = "properties";
    @Override public @NotNull JsonElement getAsJsonElement(){
        JsonObject object = new JsonObject();
        object.add(superJsonId, super.getAsJsonElement());
        object.add(propertiesId, subConfigs.getAsJsonElement());
        return object;
    }
    @Override public void setValueFromJsonElement(@NotNull JsonElement element){
        if(element instanceof JsonObject object
            && object.get(superJsonId) instanceof JsonElement superElement
            && object.get(propertiesId) instanceof JsonElement propertiesElement){
            super.setValueFromJsonElement(superElement);
            subConfigs.setValueFromJsonElement(propertiesElement);
        }
        else warnFailedLoadingConfig(this, element);
    }
    private boolean lastValue;
    private final LPCConfigList subConfigs;
}
