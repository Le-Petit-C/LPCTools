package lpctools.script;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueBooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

// 显示在malilib配置界面中的脚本配置项
public class ScriptConfig extends WrappedThirdListConfig implements AutoCloseable{
	public final Script script;
	public final UniqueBooleanConfig isEnabled = new UniqueBooleanConfig(this, "enabled", false, null){
		@Override public void getButtonOptions(ButtonOptionArrayList res) {
			res.add(new ButtonOption(
				1,
				((button, mouseButton) -> toggleBooleanValue()),
				()-> "lpctools.configs.scripts.scripts." + (getBooleanValue() ? "enabled" : "disabled"),
				buttonGenericAllocator)
			);
		}
	};
	public final UniqueStringConfig scriptId = new UniqueStringConfig(this, "id", "", null);
	public final ButtonConfig editButton = new ButtonConfig(this, "edit");
	public final ButtonConfig exceptionButton = new ButtonConfig(this, "exception"){
		@Override public void getButtonOptions(ButtonOptionArrayList res) {
			buttonName = script.hasExceptions() ?
				"lpctools.configs.scripts.scripts.script.exception.titleWithExceptions" :
				"lpctools.configs.scripts.scripts.script.exception.title";
			super.getButtonOptions(res);
		}
	};
	public ScriptConfig(@NotNull ILPCConfigReadable parent) {
		super(parent, "script", null);
		addConfig(isEnabled);
		addConfig(editButton);
		addConfig(exceptionButton);
		script = new Script(this);
		scriptId.setValueFromString(script.getId());
		scriptId.setValueChangeCallback(()->script.setId(scriptId.getStringValue()));
		isEnabled.setValueChangeCallback(()->script.enable(isEnabled.getBooleanValue()));
		editButton.setListener((button, mouseButton) -> script.openEditScreen());
		exceptionButton.setListener((button, mouseButton) -> script.clearExceptions());
	}
	
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		if(!isExpanded()) {
			if(script.hasExceptions()) {
				res.add(new ButtonOption(
					1, null,
					()->Text.translatable("lpctools.configs.scripts.scripts.script.exception.smallName").getString(),
					buttonGenericAllocator)
				);
			}
			else {
				res.add(new ButtonOption(
					-1,
					((button, mouseButton) -> isEnabled.toggleBooleanValue()),
					()-> (isEnabled.getBooleanValue() ?"§2O": "§4X"),
					buttonGenericAllocator)
				);
			}
		}
		scriptId.getButtonOptions(res);
	}
	@Override public @NotNull JsonObject getAsJsonElement() {
		var res = script.getAsJsonElement();
		res.addProperty(expandedKey, isExpanded());
		return res;
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {
		script.setValueFromJsonElement(data);
		if(data instanceof JsonObject object && object.get(expandedKey) instanceof JsonPrimitive primitive)
			setExpanded(primitive.getAsBoolean());
		return new UpdateTodo().valueChanged();
	}
	
	@Override public void close() {script.close();}
}
