package lpctools.script.suppliers.ControlFlow;

import com.google.gson.JsonElement;
import lpctools.mixin.client.BlockReplaceAction;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoAttack extends AbstractScript implements IControlFlowSupplier {
	public DoAttack(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		return map->{
			((BlockReplaceAction) MinecraftClient.getInstance()).invokeDoAttack();
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
