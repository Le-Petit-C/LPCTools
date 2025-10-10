package lpctools.script.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;
import static lpctools.script.trigger.TriggerOption.triggerOptionFactories;

//TODO:选择添加触发选项
//TODO:AutoCloseable清理
//脚本触发器
public class ScriptTrigger extends AbstractScriptWithSubScript implements IScriptWithSubScript, AutoCloseable, IButtonActionListener {
	public final Script script;
	public ScriptTrigger(Script script){
		this.script = script;
	}
	@Override public @NotNull IScriptWithSubScript getParent() {return script;}
	@Override public @NotNull Script getScript() {return script;}
	private final ArrayList<TriggerOption> triggers = new ArrayList<>();
	private final ButtonBase AddTriggerOptionButton = new ButtonGeneric(0, 0, 20, 20, "+").setActionListener(this);
	private final Iterable<?> widgets = List.of(AddTriggerOptionButton);
	
	public static final String triggerKeyJsonKey = "key";
	public static final String triggerValueJsonKey = "value";
	
	@Override public @Nullable String getName() {
		return Text.translatable("lpctools.script.trigger").getString();
	}
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonArray array = new JsonArray();
		for(var trigger : triggers){
			JsonObject object = new JsonObject();
			object.addProperty(triggerKeyJsonKey, trigger.getFactory().getKey());
			object.add(triggerValueJsonKey, trigger.getAsJsonElement());
			array.add(object);
		}
		return array;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		// Trigger的loadFromJsonElement前Script都会保证已经disabled，但是仍然检测以防万一
		if(script.isEnabled()) registerAll(false);
		//清空当前配置
		triggers.clear();
		//加载配置
		if(element instanceof JsonArray array){
			array.forEach(e->{
				if(!(e instanceof JsonObject object)) return;
				if(!(object.get(triggerKeyJsonKey) instanceof JsonPrimitive primitive)) return;
				String key = primitive.getAsString();
				TriggerOption.TriggerOptionFactory factory = triggerOptionFactories.get(key);
				var optionJson = object.get(triggerValueJsonKey);
				if(factory != null) {
					TriggerOption option = factory.allocateOption(this);
					option.setValueFromJsonElement(optionJson);
					triggers.add(option);
				}
				else warnFailedLoadingConfig(getName() + "." + key, optionJson);
			});
		}
		else if(element != null)
			warnFailedLoadingConfig(getName(), element);
		if(script.isEnabled()) registerAll(true);
	}
	public void registerAll(boolean register) {
		triggers.forEach((option)->option.registerScript(register));
	}
	@Override public @NotNull List<? extends TriggerOption> getSubScripts() {return triggers;}
	
	@Override public boolean isSubScriptMutable() {return true;}
	
	@Override public void close() {while (!triggers.isEmpty()) triggers.removeLast().close();}
	@Override public @Nullable Iterable<?> getWidgets() {return widgets;}
	@Override public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
		HashSet<TriggerOption.TriggerOptionFactory> containedFactories = new HashSet<>();
		triggers.forEach(triggerOption->{
			var factory = triggerOption.getFactory();
			if(factory.allowMulti()) return;
			containedFactories.add(factory);
		});
		HashMap<String, ChooseScreen.OptionCallback<ScriptTrigger>> options = new HashMap<>();
		HashMap<String, String> chooseTree = new HashMap<>();
		triggerOptionFactories.forEach((key, factory)->{
			if(containedFactories.contains(factory)) return;
			options.put(key, (b, m, u)->{
				u.triggers.add(factory.allocateOption(u));
				u.getDisplayWidget().markUpdateChain();
			});
			chooseTree.put("lpctools.script.trigger." + key, key);
		});
		ChooseScreen.openChooseScreen(
			Text.translatable("lpctools.script.trigger.chooseScreen.title").getString(),
			true, true, options, chooseTree, this
		);
	}
}
