package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.script.AbstractScriptWithSubScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.Boolean.And;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ForLoop extends AbstractScriptWithSubScript implements IControlFlowIssueSupplier {
	public final RunMultiple initialization = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.controlFlowIssue.forLoop.initialization.name"));
	public final And condition = new And(this, Text.translatable("lpctools.script.suppliers.controlFlowIssue.forLoop.condition.name"));
	public final RunMultiple update = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.controlFlowIssue.forLoop.update.name"));
	public final RunMultiple loopBody = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.controlFlowIssue.forLoop.loopBody.name"));
	public static final String initializationJsonKey = "initialization";
	public static final String conditionJsonKey = "condition";
	public static final String updateJsonKey = "update";
	public static final String loopBodyJsonKey = "loopBody";
	
	public ForLoop(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledInitialization = initialization.compileCheckedNotNull(environment);
		var compiledCondition = condition.compileCheckedNotNull(environment);
		var compiledUpdate = update.compileCheckedNotNull(environment);
		var compiled = loopBody.compileCheckedNotNull(environment);
		return map->{
			var initializationFlow = compiledInitialization.scriptApply(map);
			if(initializationFlow.shouldEndRunMultiple){
				if(initializationFlow != ControlFlowIssue.RETURN) return ControlFlowIssue.RETURN;
				else throw ScriptRuntimeException.illegalControlFlow(this);
			}
			while (compiledCondition.scriptApply(map)) {
				var flow = compiled.scriptApply(map);
				if (flow.shouldBreak) return flow.applied();
				var updateFlow = compiledUpdate.scriptApply(map);
				if (updateFlow.shouldEndRunMultiple) {
					if (updateFlow != ControlFlowIssue.RETURN) return ControlFlowIssue.RETURN;
					else throw ScriptRuntimeException.illegalControlFlow(this);
				}
			}
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.add(initializationJsonKey, initialization.getAsJsonElement());
		res.add(conditionJsonKey, condition.getAsJsonElement());
		res.add(updateJsonKey, update.getAsJsonElement());
		res.add(loopBodyJsonKey, loopBody.getAsJsonElement());
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig("runIfElse", element);
			return;
		}
		initialization.setValueFromJsonElement(object.get(initializationJsonKey));
		condition.setValueFromJsonElement(object.get(conditionJsonKey));
		update.setValueFromJsonElement(object.get(updateJsonKey));
		loopBody.setValueFromJsonElement(object.get(loopBodyJsonKey));
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {
		return List.of(initialization, condition, update, loopBody);
	}
}
