package lpctools.scripts.runners;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BooleanSupplierChooser;
import lpctools.scripts.choosers.RunnerChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class RunIfElse extends ThirdListConfig implements IScriptRunner {
	public final BooleanSupplierChooser condition;
	public final RunnerChooser runIf, runElse;
	public RunIfElse(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		condition = new BooleanSupplierChooser(parent, "condition", this::onValueChanged);
		runIf = new RunnerChooser(parent, "runIf", this::onValueChanged);
		runElse = new RunnerChooser(parent, "runElse", this::onValueChanged);
		refreshConfigList();
	}
	private void refreshConfigList(){
		getConfigs().clear();
		addConfig(condition.get());
		addConfig(runIf.get());
		addConfig(runElse.get());
	}
	private ThirdListConfig prepareJson(){
		ThirdListConfig list = new ThirdListConfig(getParent(), nameKey, null);
		list.setExpanded(isExpanded());
		list.addConfig(condition);
		list.addConfig(runIf);
		list.addConfig(runElse);
		return list;
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> condition.openChoose(), ()->fullKey + ".condition", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> runIf.openChoose(), ()->fullKey + ".runIf", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> runElse.openChoose(), ()->fullKey + ".runElse", buttonGenericAllocator);
	}
	@Override public @NotNull Consumer<CompiledVariableList>
	compile(VariableMap variableMap) throws CompileFailedException {
		ToBooleanFunction<CompiledVariableList> condition;
		Consumer<CompiledVariableList> runIf, runElse;
		condition = this.condition.get().compileToBoolean(variableMap);
		runIf = this.runIf.get().compile(variableMap);
		runElse = this.runElse.get().compile(variableMap);
		return list->{
			if(condition.applyAsBoolean(list)) runIf.accept(list);
			else runElse.accept(list);
		};
	}
	@Override public @NotNull JsonObject getAsJsonElement() {
		return prepareJson().getAsJsonElement();
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		ThirdListConfig config = prepareJson();
		UpdateTodo todo = config.setValueFromJsonElementEx(element);
		setExpanded(config.isExpanded());
		return todo;
	}
	@Override public void onValueChanged() {
		refreshConfigList();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "runIfElse";
	public static final String fullKey = fullPrefix + nameKey;
}
