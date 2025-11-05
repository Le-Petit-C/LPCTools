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

public class DoWhileLoop extends AbstractScriptWithSubScript implements IControlFlowSupplier {
	RunMultiple loopBody = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.doWhileLoop.loopBody.name"));
	And condition = new And(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.doWhileLoop.condition.name"));
	public static final String conditionJsonKey = "condition";
	public static final String loopBodyJsonKey = "loopBody";
	
	public DoWhileLoop(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiled = loopBody.compile(variableMap);
		var compiledCondition = condition.compile(variableMap);
		return map->{
			while(true){
				var flow = compiled.scriptApply(map);
				if(flow.shouldBreak) return flow.applied();
				var conditionResult = compiledCondition.scriptApply(map);
				if(conditionResult == null) throw ScriptRuntimeException.nullPointer(this);
				if(!conditionResult) break;
			}
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.add(loopBodyJsonKey, loopBody.getAsJsonElement());
		res.add(conditionJsonKey, condition.getAsJsonElement());
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig("runIfElse", element);
			return;
		}
		loopBody.setValueFromJsonElement(object.get(loopBodyJsonKey));
		condition.setValueFromJsonElement(object.get(conditionJsonKey));
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {
		return List.of(condition, loopBody);
	}
}
