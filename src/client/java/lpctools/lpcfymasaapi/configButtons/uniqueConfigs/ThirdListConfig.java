package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

import static lpctools.lpcfymasaapi.LPCConfigUtils.*;

//第三级列表，配置中切换true或false可以展开或收起内含的配置项
public class ThirdListConfig extends LPCUniqueConfigBase implements IThirdListBase, IBooleanThirdList {
    protected boolean expanded;
    public final String expandedJsonId = "expanded";
    public ThirdListConfig(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        subConfigs = new LPCConfigList(parent, nameKey);
    }
    @Override public void onValueChanged() {
        getPage().updateIfCurrent();
        super.onValueChanged();
    }
    @Override public @NotNull Collection<ILPCConfig> getConfigs() {return subConfigs.getConfigs();}
    @Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper> buildConfigWrappers(ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList) {
        if(isExpanded()) return subConfigs.buildConfigWrappers(wrapperList);
        else return wrapperList;
    }
    @Override public @NotNull JsonObject getAsJsonElement(){
        JsonObject object = new JsonObject();
        object.addProperty(expandedJsonId, expanded);
        object.add(propertiesId, subConfigs.getAsJsonElement());
        return object;
    }
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element){
        UpdateTodo todo = new UpdateTodo();
        if(element instanceof JsonObject object){
            if(object.get(expandedJsonId) instanceof JsonPrimitive primitive){
                todo.valueChanged(expanded != primitive.getAsBoolean());
                expanded = primitive.getAsBoolean();
            }
            if(object.get(propertiesId) instanceof JsonElement propertiesElement)
                todo.combine(subConfigs.setValueFromJsonElementEx(propertiesElement));
        }
        else warnFailedLoadingConfig(this, element);
        return todo;
    }
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        res.add(new ButtonOption(-1, (button, mouseButton) -> setExpanded(!isExpanded()), null,
            ILPCUniqueConfigBase.iconButtonAllocator(expanded ? MaLiLibIcons.ARROW_UP : MaLiLibIcons.ARROW_DOWN, LeftRight.CENTER)));
    }
    @Override public boolean isExpanded() {return expanded;}
    @Override public void setExpanded(boolean expanded) {
        if(expanded != this.expanded){
            this.expanded = expanded;
            getPage().updateIfCurrent();
            onValueChanged();
        }
    }
    private final LPCConfigList subConfigs;
}
