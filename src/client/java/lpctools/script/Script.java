package lpctools.script;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.util.FileUtils;
import lpctools.script.editScreen.ScriptEditScreen;
import lpctools.script.editScreen.WidthAutoAdjustTextField;
import lpctools.script.exceptions.ScriptException;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptRunnable;
import lpctools.script.suppliers.ControlFlowIssue.RunMultiple;
import lpctools.script.trigger.ScriptTrigger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;
import static lpctools.script.ScriptsConfig.scriptDirectoryName;

public class Script extends AbstractScriptWithSubScript implements IScriptWithSubScript, AutoCloseable {
	private @Nullable ScriptEditScreen editScreen;
	public final ScriptConfig config;
	private boolean enabled = false;
	private final ScriptTrigger trigger = new ScriptTrigger(this);
	private final RunMultiple operations = new RunMultiple(this, Text.translatable("lpctools.script.operations.name"));
	private final List<IScript> subScripts = List.of(trigger, operations);
	private @Nullable WidthAutoAdjustTextField idWidget;
	private @Nullable List<Object> widgets;
	private @NotNull String id = "";
	private static final String triggerJsonKey = "trigger";
	private static final String operationsJsonKey = "operations";
	private static final String enableJsonKey = "enabled";
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
			idWidget = new WidthAutoAdjustTextField(getDisplayWidget(), 100, id, text->{
				config.scriptId.setValueFromString(text);
				config.getPage().markNeedUpdate();
			});
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
	public @NotNull String getId(){return id;}
	public void setId(String id){
		if(id.equals(this.id)) return;
		Text warnText = checkId(id);
		if(warnText != null){
			var pageInstance = config.getPage().getPageInstance();
			if(pageInstance != null) pageInstance.cursorInfo(warnText, 3000);
			if(editScreen != null) editScreen.cursorInfo(warnText, 3000);
			config.scriptId.setValueFromString(this.id);
			return;
		}
		if(!this.id.isEmpty()) {
			Path scriptDirectoryPath = FileUtils.getConfigDirectoryAsPath().resolve(scriptDirectoryName);
			Path oldPath = scriptDirectoryPath.resolve(this.id + ".json");
			Path newPath = scriptDirectoryPath.resolve(id + ".json");
			FileUtils.renameFile(oldPath, newPath, s->{});
			ScriptsConfig.instance.existScript.remove(this.id);
		}
		this.id = id;
		ScriptsConfig.instance.existScript.add(this.id);
		if(idWidget != null) idWidget.setText(id);
		config.scriptId.setValueFromString(id);
	}
	
	private static @Nullable Text checkId(String id) {
		final String invalidCharacters = "\\/*?\"<>|";
		if(ScriptsConfig.instance.existScript.contains(id))
			return Text.translatable("lpctools.script.exception.id.scriptNameRepeat");
		else {
			boolean hasInvalidCharacter = false;
			for(int i = 0; i < invalidCharacters.length(); ++i){
				if (id.contains(invalidCharacters.substring(i, i + 1))) {
					hasInvalidCharacter = true;
					break;
				}
			}
			if(hasInvalidCharacter) return Text.translatable("lpctools.script.exception.id.invalidCharacter");
			else if(id.endsWith(" ") || id.endsWith("."))
				return Text.translatable("lpctools.script.exception.id.invalidEnd");
			else return null;
		}
	}
	
	@Override public @NotNull JsonObject getAsJsonElement() {
		JsonObject object = new JsonObject();
		object.add(triggerJsonKey, trigger.getAsJsonElement());
		object.add(operationsJsonKey, operations.getAsJsonElement());
		object.addProperty(enableJsonKey, enabled);
		return object;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element instanceof JsonObject object){
			if(enabled) enable(false);
			trigger.setValueFromJsonElement(object.get(triggerJsonKey));
			operations.setValueFromJsonElement(object.get(operationsJsonKey));
			if(object.get(enableJsonKey) instanceof JsonPrimitive enableJson)
				if(enableJson.getAsBoolean()) enable(true);
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
				enable(false);
				//markNeedRecompile();
				//不标记重新编译，避免无限异常
			}
		}
		else enable(false);
	}
	
	public boolean hasExceptions(){return !exceptions.isEmpty();}
	public void putException(IScript source, ScriptException exception){
		exceptions.computeIfAbsent(source, v->new ArrayList<>()).add(exception);
		config.getPage().markNeedUpdate();
	}
	public void clearExceptions(){
		exceptions.clear();
		markNeedRecompile();
		config.getPage().markNeedUpdate();
	}
	
	private ScriptRunnable compile(){
		var environment = new CompileEnvironment();
		var func = operations.compile(environment);
		var runtimeVariableMapSupplier = environment.createRuntimeVariableMapSupplier();
		needRecompile = false;
		return ()->func.scriptApply(runtimeVariableMapSupplier.get());
	}
	
	@Override public void close() {trigger.close();}
}
