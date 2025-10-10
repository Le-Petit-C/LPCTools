package lpctools.script.trigger;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.InputHandler;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.WidthAutoAdjustButtonKeybind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class HotkeyOption extends TriggerOptionBase {
	private @Nullable WidthAutoAdjustButtonKeybind keybindButton;
	static class HotkeyOptionFactory implements TriggerOptionFactory {
		@Override
		public TriggerOption allocateOption(ScriptTrigger trigger) {return new HotkeyOption(trigger, this);}
		
		@Override
		public String getKey() {return "hotkey";}
		
		@Override
		public boolean allowMulti() {return true;}
	}
	
	private final IKeybind hotkey = KeybindMulti.fromStorageString("", KeybindSettings.DEFAULT);
	private @Nullable List<ConfigButtonKeybind> buttons;
	
	private HotkeyOption(ScriptTrigger trigger, HotkeyOptionFactory factory) {super(trigger, factory);}
	
	@Override public void registerScript(boolean b) {
		InputHandler inputHandler = LPCTools.page.getInputHandler();
		if (b) inputHandler.addKeybind(hotkey);
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
	
	@Override public @NotNull IScriptWithSubScript getParent() {return trigger;}
}
