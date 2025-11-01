package lpctools.script;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.editScreen.ScriptEditScreen;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.exceptions.ScriptException;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptRunnable;
import lpctools.script.suppliers.Void.NamedRunMultiple;
import lpctools.script.trigger.ScriptTrigger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class Script extends AbstractScriptWithSubScript implements IScriptWithSubScript {
	private @Nullable ScriptEditScreen editScreen;
	public final ScriptConfig config;
	private boolean enabled = false;
	private final ScriptTrigger trigger = new ScriptTrigger(this);
	private final NamedRunMultiple operations = new NamedRunMultiple(this, Text.translatable("lpctools.script.operations.name"));
	private final List<IScript> subScripts = List.of(trigger, operations);
	private @Nullable WidthAutoAdjustTextField idWidget;
	private @Nullable List<Object> widgets;
	private String id = "script";
	private static final String triggerJsonKey = "trigger";
	private static final String operationsJsonKey = "operations";
	private static final String enableJsonKey = "enabled";
	private static final String idJsonKey = "id";
	private boolean needRecompile = true;
	private @Nullable ScriptRunnable runnable;
	private final HashMap<IScript, ArrayList<ScriptException>> exceptions = new HashMap<>();
	
	public void markNeedRecompile(){needRecompile = true;}
	public boolean isEnabled() {return enabled;}
	public Script(ScriptConfig config){
		super(null);
		this.config = config;
	}
	public @NotNull WidthAutoAdjustTextField getIdWidget(){
		if(idWidget == null){
			idWidget = new WidthAutoAdjustTextField(getDisplayWidget(), 100, text->{
				config.scriptId.setValueFromString(text);
				config.getPage().markNeedUpdate();
			});
			idWidget.setText(id);
		}
		return idWidget;
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
		if(this.enabled != enable){
			this.enabled = enable;
			config.isEnabled.setBooleanValue(enable);
			trigger.registerAll(enable);
		}
	}
	@Override public @Nullable Text getName() {return null;}
	public String getId(){return id;}
	public void setId(String id){
		if(id.equals(this.id)) return;
		this.id = id;
		if(idWidget != null) idWidget.setText(id);
		config.scriptId.setValueFromString(id);
	}
	@Override public @NotNull JsonObject getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.add(triggerJsonKey, trigger.getAsJsonElement());
		object.add(operationsJsonKey, operations.getAsJsonElement());
		object.addProperty(enableJsonKey, enabled);
		object.addProperty(idJsonKey, id);
		return object;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element instanceof JsonObject object){
			if(enabled) enable(false);
			trigger.setValueFromJsonElement(object.get(triggerJsonKey));
			operations.setValueFromJsonElement(object.get(operationsJsonKey));
			if(object.get(enableJsonKey) instanceof JsonPrimitive enableJson)
				if(enableJson.getAsBoolean()) enable(true);
			if(object.get(idJsonKey) instanceof JsonPrimitive nameJson)
				setId(nameJson.getAsString());
		}
		else if(element != null) warnFailedLoadingConfig(getName(), element);
	}
	@Override public @NotNull List<IScript> getSubScripts() {return subScripts;}
	
	@Override public @Nullable Iterable<Object> getWidgets() {
		if(widgets == null) widgets = List.of(getIdWidget());
		return widgets;
	}
	@Override public @NotNull Script getScript(){return this;}
	
	//运行脚本
	public void runScript(){
		if(needRecompile) runnable = compile();
		if(runnable != null) {
			try{
				runnable.scriptRun();
			} catch (ScriptRuntimeException e){
				runnable = null;
				//markNeedRecompile();
				//不标记重新编译，避免无限异常
			}
		}
	}
	
	public boolean hasExceptions(){return !exceptions.isEmpty();}
	public void putException(IScript source, ScriptException exception){
		exceptions.computeIfAbsent(source, v->new ArrayList<>()).add(exception);
		config.getPage().markNeedUpdate();
	}
	public void clearExceptions(){
		exceptions.clear();
		config.getPage().markNeedUpdate();
	}
	
	private ScriptRunnable compile(){
		var environment = new CompileEnvironment();
		var func = operations.compile(environment);
		var runtimeVariableMapSupplier = environment.createRuntimeVariableMapSupplier();
		needRecompile = false;
		return ()->func.scriptApply(runtimeVariableMapSupplier.get());
	}
}
