package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.IExpandableThirdList;
import lpctools.lpcfymasaapi.interfaces.ILPCConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class StringThirdListConfig extends UniqueStringConfig implements IExpandableThirdList {
	boolean expanded;
	public final LPCConfigList subConfigs;
	public StringThirdListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable String defaultString, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, defaultString, callback);
		subConfigs = new LPCConfigList(parent, nameKey);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(thirdListIconPreset());
		super.getButtonOptions(res);
	}
	@Override public boolean isExpanded() {return expanded;}
	@Override public void setExpanded(boolean expanded) {
		if(this.expanded != expanded){
			this.expanded = expanded;
			getPage().updateIfCurrent();
		}
	}
	@Override public @NotNull Collection<ILPCConfig> getConfigs() {return subConfigs.getConfigs();}
	@Override public @Nullable JsonObject getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.add(propertiesId, subConfigs.getAsJsonElement());
		object.add("strings", super.getAsJsonElement());
		object.addProperty(expandedKey, expanded);
		return object;
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
		if(data instanceof JsonObject object){
			UpdateTodo updateTodo = new UpdateTodo();
			updateTodo.combine(super.setValueFromJsonElementEx(object.get("strings")));
			if(object.get(expandedKey) instanceof JsonPrimitive primitive){
				boolean b = expanded;
				expanded = primitive.getAsBoolean();
				updateTodo.updatePage(b != expanded);
			}
			updateTodo.combine(subConfigs.setValueFromJsonElementEx(object.get(propertiesId)));
			return updateTodo;
		}
		return setValueFailed(data);
	}
}
