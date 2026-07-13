package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.ToIntFunction;

public class WrappedThirdListConfig extends LPCUniqueConfigBase implements ILPCConfigReadable, IExpandableThirdList {
	@NotNull ArrayList<ILPCConfig> configs = new ArrayList<>();
	public WrappedThirdListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {super(parent, nameKey, callback);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(IExpandableThirdList.thirdListIconPreset(this));
	}
	@Override public @NotNull Collection<ILPCConfig> getConfigs() {return configs;}
	int indent;
	@Override public void setAlignedIndent(int indent) {
		this.indent = indent;
		for(ILPCConfig config : configs)
			if(config instanceof ILPCConfigReadable readable)
				readable.setAlignedIndent(indent);
	}
	@Override public int getAlignedIndent() {return indent;}
	boolean expanded;
	@Override public boolean isExpanded() {return expanded;}
	@Override public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		getPage().markNeedUpdate();
	}
	@Override public ArrayList<GuiConfigsBase.ConfigOptionWrapper>
	buildConfigWrappers(ToIntFunction<String> getStringWidth, ArrayList<GuiConfigsBase.ConfigOptionWrapper> wrapperList){
		if(!expanded) return wrapperList;
		int indent = 0;
		for(ILPCConfig config : getConfigs()){
			config.refreshName();
			if(config instanceof ILPCConfigReadable list){
				list.buildConfigWrappers(getStringWidth, wrapperList);
				indent = Math.max(indent, list.getAlignedIndent());
			}
			else {
				wrapperList.add(new GuiConfigsBase.ConfigOptionWrapper(config));
				indent = Math.max(indent, getStringWidth.applyAsInt(config.getConfigGuiDisplayName()));
			}
		}
		setAlignedIndent(indent);
		return wrapperList;
	}
	
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
		if(data instanceof JsonObject object){
			UpdateTodo todo = new UpdateTodo();
			if(object.get(propertiesId) instanceof JsonElement element)
				todo.combine(IExpandableThirdList.super.setValueFromJsonElementEx(element));
			if(object.get("expanded") instanceof JsonPrimitive primitive){
				boolean lastExpanded = expanded;
				expanded = primitive.getAsBoolean();
				todo.valueChanged(lastExpanded != expanded);
			}
			return todo;
		}
		else return setValueFailed(data);
	}
	
	@Override public @Nullable JsonObject getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.add(propertiesId, IExpandableThirdList.super.getAsJsonElement());
		object.addProperty("expanded", expanded);
		return object;
	}
}
