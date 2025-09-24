package lpctools.script;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueBooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;

// 显示在malilib配置界面中的脚本配置项
public class ScriptConfig extends WrappedThirdListConfig{
	public final Script script;
	public final UniqueBooleanConfig isEnabled = new UniqueBooleanConfig(this, "enabled", false, null){
		@Override
		public void getButtonOptions(ButtonOptionArrayList res) {
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
	public ScriptConfig(@NotNull ILPCConfigReadable parent) {
		super(parent, "script", null);
		addConfig(isEnabled);
		addConfig(scriptId);
		addConfig(editButton);
		script = new Script(this);
		scriptId.setValueChangeCallback(()->script.id.setText(scriptId.getStringValue()));
		isEnabled.setValueChangeCallback(()->script.enable(isEnabled.getBooleanValue()));
		editButton.setListener((button, mouseButton) -> script.openEditScreen());
	}
	
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		if(!isExpanded()) {
			res.add(new ButtonOption(
				-1,
				((button, mouseButton) -> isEnabled.toggleBooleanValue()),
				()-> (isEnabled.getBooleanValue() ?"§2O": "§4X"),
				buttonGenericAllocator)
			);
			scriptId.getButtonOptions(res);
		}
	}
}
