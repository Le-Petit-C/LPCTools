package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.IThirdListBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

public class ButtonThirdListConfig extends ButtonConfig implements IThirdListBase {
    public boolean extended = false;
    public final LPCConfigList subConfigs;
    public ButtonThirdListConfig(ILPCConfigList parent, String nameKey, @Nullable IButtonActionListener listener) {
        super(parent, nameKey, listener);
        subConfigs = new LPCConfigList(this, getNameKey());
    }
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(extended) return subConfigs.buildConfigWrappers(wrapperList);
        else return wrapperList;
    }
    @Override public @NotNull ArrayList<ILPCConfig> getConfigs() {return subConfigs.getConfigs();}
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
        res.add(new ButtonOption(-1, (button, mouseButton)->{extended = !extended; getPage().updateIfCurrent();}, null,
            ILPCUniqueConfigBase.iconButtonAllocator(extended ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER)));
        super.getButtonOptions(res);
    }
    
    @Override public void setValueFromJsonElement(@NotNull JsonElement data) {
        if(data instanceof JsonObject object){
            if(object.get(propertiesId) instanceof JsonElement element)
                subConfigs.setValueFromJsonElement(element);
            if(object.get("expanded") instanceof JsonPrimitive primitive)
                extended = primitive.getAsBoolean();
            onValueChanged();
        }
        else warnFailedLoadingConfig(this, data);
    }
    @Override public @Nullable JsonObject getAsJsonElement() {
        JsonObject object = new JsonObject();
        object.addProperty("expanded", extended);
        object.add(propertiesId, subConfigs.getAsJsonElement());
        return object;
    }
}
