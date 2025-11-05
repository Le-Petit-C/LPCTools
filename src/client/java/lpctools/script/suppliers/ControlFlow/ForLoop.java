package lpctools.script.suppliers.ControlFlow;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.script.AbstractScriptWithSubScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.Boolean.And;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ForLoop extends AbstractScriptWithSubScript implements IControlFlowSupplier {
	RunMultiple initialization = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.forLoop.initialization.name"));
	And condition = new And(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.forLoop.condition.name"));
	RunMultiple update = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.forLoop.update.name"));
	RunMultiple loopBody = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.forLoop.loopBody.name"));
	public static final String initializationJsonKey = "initialization";
	public static final String conditionJsonKey = "condition";
	public static final String updateJsonKey = "update";
	public static final String loopBodyJsonKey = "loopBody";
	
	public ForLoop(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledInitialization = initialization.compile(variableMap);
		var compiledCondition = condition.compile(variableMap);
		var compiledUpdate = update.compile(variableMap);
		var compiled = loopBody.compile(variableMap);
		return map->{
			var initializationFlow = compiledInitialization.scriptApply(map);
			if(initializationFlow.shouldEndRunMultiple){
				if(initializationFlow != ControlFlowIssue.RETURN) return ControlFlowIssue.RETURN;
				else throw ScriptRuntimeException.illegalControlFlow(this);
			}
			while(true){
				var conditionResult = compiledCondition.scriptApply(map);
				if(conditionResult == null) throw ScriptRuntimeException.nullPointer(this);
				if(!conditionResult) break;
				var flow = compiled.scriptApply(map);
				if(flow.shouldBreak) return flow.applied();
				var updateFlow = compiledUpdate.scriptApply(map);
				if(updateFlow.shouldEndRunMultiple){
					if(updateFlow != ControlFlowIssue.RETURN) return ControlFlowIssue.RETURN;
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
