package lpctools.script;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.editScreen.ScriptEditScreen;
import lpctools.script.editScreen.ScriptFitTextField;
import lpctools.script.trigger.ScriptTrigger;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class Script extends AbstractScriptWithSubScript implements IScriptWithSubScript {
	private @Nullable ScriptEditScreen editScreen;
	public final ScriptConfig config;
	private String name = "script";
	private boolean enabled = false;
	private final ScriptTrigger trigger = new ScriptTrigger(this);
	private final List<IScript> subScripts = List.of(trigger);
	private @Nullable ScriptFitTextField id;
	private @Nullable List<Object> widgets;
	public boolean isEnabled() {return enabled;}
	public Script(ScriptConfig config){this.config = config;}
	public @NotNull ScriptFitTextField getId(){
		if(id == null)
			id = new ScriptFitTextField(getDisplayWidget(), 100, text->{
				config.scriptId.setValueFromString(text);
				config.getPage().markNeedUpdate();
			});
		return id;
	}
	public @NotNull ScriptEditScreen getEditScreen(){
		if(editScreen == null) editScreen = new ScriptEditScreen(this);
		return editScreen;
	}
	public void openEditScreen() {
		var screen = getEditScreen();
		screen.setParent(MinecraftClient.getInstance().currentScreen);
		MinecraftClient.getInstance().setScreen(screen);
	}
	//启用脚本
	public void enable(boolean enable) {
		this.enabled = enable;
		trigger.registerAll(enable);
	}
	@Override public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	private static final String triggerJsonKey = "trigger";
	private static final String enableJsonKey = "enabled";
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.add(triggerJsonKey, trigger.getAsJsonElement());
		object.addProperty(enableJsonKey, enabled);
		return object;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element instanceof JsonObject object){
			if(enabled) enable(false);
			trigger.setValueFromJsonElement(object.get(triggerJsonKey));
			if(object.get(enableJsonKey) instanceof JsonPrimitive enableJson)
				if(enableJson.getAsBoolean()) enable(true);
		}
		else if(element != null) warnFailedLoadingConfig(getName(), element);
	}
	@Override public @NotNull List<IScript> getSubScripts() {return subScripts;}
	
	@Override public boolean isSubScriptMutable() {return false;}
	
	@Override public @Nullable Iterable<Object> getWidgets() {
		if(widgets == null)
			widgets = List.of(getId());
		return widgets;
	}
	@Override public @Nullable IScriptWithSubScript getParent() {return null;}
	@Override public @NotNull Script getScript(){return this;}
	//运行脚本
	public void runScript(){//TODO
	
	}
	
}
