package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoNothing extends AbstractScript implements IControlFlowSupplier {
	public DoNothing(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNotNullFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compileNotNull(CompileEnvironment variableMap) {return map-> ControlFlowIssue.NO_ISSUE;}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
