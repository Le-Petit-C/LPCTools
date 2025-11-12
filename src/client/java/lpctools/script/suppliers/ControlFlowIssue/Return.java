package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Return extends AbstractScript implements IControlFlowSupplier {
	public Return(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {return map-> ControlFlowIssue.RETURN;}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
