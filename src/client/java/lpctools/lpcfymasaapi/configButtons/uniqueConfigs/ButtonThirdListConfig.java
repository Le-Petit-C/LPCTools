package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

@SuppressWarnings("unused")
public class ButtonThirdListConfig extends ButtonConfig implements IThirdListBase {
    public boolean extended = false;
    public final LPCConfigList subConfigs;
    public ButtonThirdListConfig(ILPCConfigReadable parent, String nameKey, @Nullable IButtonActionListener listener) {
        super(parent, nameKey, listener);
        subConfigs = new LPCConfigList(parent, getNameKey());
    }
    @Override public @NotNull ArrayList<ILPCConfig> getConfigs() {return subConfigs.getConfigs();}
    @Override public void setAlignedIndent(int indent) {subConfigs.setAlignedIndent(indent);}
    @Override public int getAlignedIndent() {return subConfigs.getAlignedIndent();}
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
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
