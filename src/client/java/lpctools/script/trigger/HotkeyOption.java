package lpctools.script.trigger;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.InputHandler;
import lpctools.script.IScriptWithSubScript;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

class HotkeyOption extends TriggerOptionBase {
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
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(buttons == null) buttons = List.of(new ConfigButtonKeybind(0, 0, 20, 20, hotkey, getScript().getEditScreen()) {
			@Override public void updateDisplayString() {
				super.updateDisplayString();
				setWidth(calculateTextButtonWidth(displayString, textRenderer, getHeight()));
			}
		});
		return buttons;
	}
	
	@Override
	public @Nullable JsonElement getAsJsonElement() {return hotkey.getAsJsonElement();}
	
	@Override
	public void setValueFromJsonElement(@Nullable JsonElement element) {hotkey.setValueFromJsonElement(element);}
	
	@Override
	public @NotNull IScriptWithSubScript getParent() {return trigger;}
}
