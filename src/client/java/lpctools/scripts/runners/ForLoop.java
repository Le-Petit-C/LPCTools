package lpctools.scripts.runners;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.BooleanSupplierChooser;
import lpctools.scripts.utils.choosers.RunnerChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.Variable;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ForLoop extends WrappedThirdListConfig implements IScriptRunner {
	public final RunnerChooser start = addConfig(new RunnerChooser(parent, "start", null));
	public final BooleanSupplierChooser condition = addConfig(new BooleanSupplierChooser(parent, "condition", null));
	public final RunnerChooser end = addConfig(new RunnerChooser(parent, "end", null));
	public final RunnerChooser run = addConfig(new RunnerChooser(parent, "run", null));
	public ForLoop(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> start.openChoose(), ()->fullKey + ".start", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> condition.openChoose(), ()->fullKey + ".condition", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> end.openChoose(), ()->fullKey + ".end", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> run.openChoose(), ()->fullKey + ".run", buttonGenericAllocator);
	}
	@Override public @NotNull Consumer<CompiledVariableList>
	compile(VariableMap variableMap) throws CompileFailedException {
		if(run.get() instanceof Variable<?>) throw CompileFailedException.definingVariableInLoop();
		if(end.get() instanceof Variable<?>) throw CompileFailedException.definingVariableInLoop();
		variableMap.push();
		Consumer<CompiledVariableList> start = this.start.get().compile(variableMap);
		ToBooleanFunction<CompiledVariableList> condition = this.condition.get().compileToBoolean(variableMap);
		Consumer<CompiledVariableList> end = this.end.get().compile(variableMap);
		Consumer<CompiledVariableList> run = this.run.get().compile(variableMap);
		variableMap.pop();
		return list->{
			list.push();
			for(start.accept(list); condition.applyAsBoolean(list); end.accept(list))
				run.accept(list);
			list.pop();
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "forLoop";
	public static final String fullKey = fullPrefix + nameKey;
}
