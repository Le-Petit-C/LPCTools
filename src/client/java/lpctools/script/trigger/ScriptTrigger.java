package lpctools.script.trigger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.script.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;
import static lpctools.script.trigger.TriggerOption.triggerOptionFactories;

//TODO:选择添加触发选项
//TODO:AutoCloseable清理
//脚本触发器
public class ScriptTrigger extends AbstractScriptWithSubScriptMutable<TriggerOption> implements AutoCloseable {
	public ScriptTrigger(Script script){super(script);}
	@Override public @NotNull Script getParent() {return (Script)super.getParent();}
	@Override public @NotNull Script getScript() {return getParent();}
	private @Nullable Iterable<?> widgets;
	
	public static final String triggerKeyJsonKey = "key";
	public static final String triggerValueJsonKey = "value";
	
	@Override public @Nullable Text getName() {return Text.translatable("lpctools.script.trigger");}
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonArray array = new JsonArray();
		for(var trigger : getSubScripts()){
			JsonObject object = new JsonObject();
			object.addProperty(triggerKeyJsonKey, trigger.getFactory().getKey());
			object.add(triggerValueJsonKey, trigger.getAsJsonElement());
			array.add(object);
		}
		return array;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		// Trigger的loadFromJsonElement前Script都会保证已经disabled，但是仍然检测以防万一
		if(getParent().isEnabled()) registerAll(false);
		//清空当前配置
		getSubScripts().clear();
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
					getSubScripts().add(option);
				}
				else warnFailedLoadingConfig(getName() + "." + key, optionJson);
			});
		}
		else if(element != null)
			warnFailedLoadingConfig(getName(), element);
		if(getParent().isEnabled()) registerAll(true);
	}
	public void registerAll(boolean register) {getSubScripts().forEach((option)->option.registerScript(register));}
	
	@Override public void close() {while (!getSubScripts().isEmpty()) getSubScripts().removeLast().close();}
	@Override public @Nullable Iterable<?> getWidgets() {
		if(widgets == null) widgets = List.of(createAddButton());
		return widgets;
	}
	
	@Override public void notifyInsertion(Consumer<TriggerOption> callback) {
		HashSet<TriggerOption.TriggerOptionFactory> containedFactories = new HashSet<>();
		getSubScripts().forEach(triggerOption->{
			var factory = triggerOption.getFactory();
			if(factory.allowMulti()) return;
			containedFactories.add(factory);
		});
		HashMap<String, ChooseScreen.OptionCallback<ScriptTrigger>> options = new HashMap<>();
		HashMap<String, String> chooseTree = new HashMap<>();
		triggerOptionFactories.forEach((key, factory)->{
			if(containedFactories.contains(factory)) return;
			options.put(key, (b, m, u)->callback.accept(factory.allocateOption(u)));
			chooseTree.put("lpctools.script.trigger." + key, key);
		});
		ChooseScreen.openChooseScreen(
			Text.translatable("lpctools.script.trigger.chooseScreen.title").getString(),
			true, true, options, chooseTree, this
		);
	}
}
