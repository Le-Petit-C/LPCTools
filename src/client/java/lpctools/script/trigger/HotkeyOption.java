package lpctools.script.trigger;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.gui.widgets.WidgetKeybindSettings;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.InputHandler;
import lpctools.script.editScreen.WidthAutoAdjustButtonKeybind;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class HotkeyOption extends TriggerOptionBase {
	private @Nullable WidthAutoAdjustButtonKeybind keybindButton;
	private @Nullable WidgetKeybindSettings keybindSettingsButton;
	
	private final IKeybind keybind = KeybindMulti.fromStorageString("", KeybindSettings.create(KeybindSettings.Context.ANY, KeyAction.PRESS, false, true, false, true));
	private @Nullable List<?> buttons;
	
	HotkeyOption(ScriptTrigger trigger, TriggerOptionFactory factory) {
		super(trigger, factory);
		var script = trigger.getScript();
		keybind.setCallback((action, key)->{
			script.runScript();
			return true;
		});
	}
	
	@Override public void registerScript(boolean b) {
		InputHandler inputHandler = LPCTools.page.getInputHandler();
		if(b) inputHandler.addKeybind(keybind);
		else inputHandler.removeKeybind(keybind);
	}
	
	public @NotNull WidthAutoAdjustButtonKeybind getKeybindButton(){
		if(keybindButton == null) keybindButton = new WidthAutoAdjustButtonKeybind(0, 0, 20, keybind, getDisplayWidget());
		return keybindButton;
	}
	
	public @NotNull WidgetKeybindSettings getKeybindSettingsButton(){
		if(keybindSettingsButton == null) {
			String name = getName() instanceof Text text ? text.getString() : "";
			var editScreen = getDisplayWidget().editScreen;
			keybindSettingsButton = new WidgetKeybindSettings(0, 0, 20, 20, keybind, name,
				editScreen.getListWidget(), getDisplayWidget().editScreen.getDialogHandler());
		}
		return keybindSettingsButton;
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(buttons == null) buttons = List.of(getKeybindButton(), getKeybindSettingsButton());
		return buttons;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return keybind.getAsJsonElement();}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		keybind.setValueFromJsonElement(element);
		if(keybindButton != null) keybindButton.updateDisplayString();
	}
}
