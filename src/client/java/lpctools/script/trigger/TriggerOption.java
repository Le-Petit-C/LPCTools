package lpctools.script.trigger;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.InputHandler;
import lpctools.script.IScript;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

public interface TriggerOption extends IScript, AutoCloseable {
	interface TriggerOptionFactory{
		TriggerOption allocateOption(ScriptTrigger trigger);
		boolean allowMulti();
		String getKey();
	}
	void registerScript(boolean b);
	TriggerOptionFactory getFactory();
	@Override default void close() {registerScript(false);}
	
	HashMap<String, TriggerOptionFactory> triggerOptionFactories = initTriggerOptionFactories();
	private static HashMap<String, TriggerOptionFactory> initTriggerOptionFactories(){
		HashMap<String, TriggerOptionFactory> res = new HashMap<>();
		putFactory(res, new HotkeyOption.HotkeyOptionFactory());
		return res;
	}
	private static void putFactory(HashMap<String, TriggerOptionFactory> map, TriggerOptionFactory factory){
		map.put(factory.getKey(), factory);
	}
}

abstract class TriggerOptionBase implements TriggerOption{
	protected final ScriptTrigger trigger;
	protected final TriggerOptionFactory factory;
	protected TriggerOptionBase(ScriptTrigger trigger, TriggerOptionFactory factory){
		this.trigger = trigger;
		this.factory = factory;
	}
	@Override public @Nullable JsonElement getAsJsonElement() {return JsonNull.INSTANCE;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
	@Override public TriggerOptionFactory getFactory() {return factory;}
	@Override public String getName() {return Text.translatable("lpctools.script.trigger." + getFactory().getKey()).getString();}
}

class HotkeyOption extends TriggerOptionBase{
	static class HotkeyOptionFactory implements TriggerOptionFactory{
		@Override public TriggerOption allocateOption(ScriptTrigger trigger) {return new HotkeyOption(trigger, this);}
		@Override public String getKey() {return "hotkey";}
		@Override public boolean allowMulti() {return true;}
	}
	private final IKeybind hotkey = KeybindMulti.fromStorageString("", KeybindSettings.DEFAULT);
	private final List<ConfigButtonKeybind> buttons = List.of(new ConfigButtonKeybind(0, 0, 20, 20, hotkey, null){
		@Override public void updateDisplayString() {
			super.updateDisplayString();
			setWidth(calculateTextButtonWidth(displayString, textRenderer, getHeight()));
		}
	});
	private HotkeyOption(ScriptTrigger trigger, HotkeyOptionFactory factory){super(trigger, factory);}
	@Override public void registerScript(boolean b) {
		InputHandler inputHandler = LPCTools.page.getInputHandler();
		if(b) inputHandler.addKeybind(hotkey);
		else inputHandler.removeKeybind(hotkey);
	}
	@Override public @Nullable Iterable<?> getWidgets() {return buttons;}
	@Override public @Nullable JsonElement getAsJsonElement() {return hotkey.getAsJsonElement();}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {hotkey.setValueFromJsonElement(element);}
	
	@Override public @NotNull IScript getParent() {return trigger;}
}
