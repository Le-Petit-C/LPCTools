package lpctools.script.trigger;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.InputHandler;
import lpctools.script.editScreen.WidthAutoAdjustButtonKeybind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class HotkeyOption extends TriggerOptionBase {
	private @Nullable WidthAutoAdjustButtonKeybind keybindButton;
	
	private final IKeybind hotkey = KeybindMulti.fromStorageString("", KeybindSettings.DEFAULT);
	private @Nullable List<ConfigButtonKeybind> buttons;
	
	HotkeyOption(ScriptTrigger trigger, TriggerOptionFactory factory) {
		super(trigger, factory);
		var script = trigger.getScript();
		hotkey.setCallback((action, key)->{
			script.runScript();
			return true;
		});
	}
	
	@Override public void registerScript(boolean b) {
		InputHandler inputHandler = LPCTools.page.getInputHandler();
		if(b) inputHandler.addKeybind(hotkey);
		else inputHandler.removeKeybind(hotkey);
	}
	
	public @NotNull WidthAutoAdjustButtonKeybind getKeybindButton(){
		if(keybindButton == null) keybindButton = new WidthAutoAdjustButtonKeybind(0, 0, 20, hotkey, getDisplayWidget());
		return keybindButton;
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(buttons == null) buttons = List.of(getKeybindButton());
		return buttons;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return hotkey.getAsJsonElement();}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		hotkey.setValueFromJsonElement(element);
		if(keybindButton != null) keybindButton.updateDisplayString();
	}
}
