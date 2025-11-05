package lpctools.script.suppliers.ControlFlow;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.script.*;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.Boolean.And;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class RunIfElse extends AbstractScriptWithSubScript implements IControlFlowSupplier {
	And condition = new And(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.runIfElse.condition.name"));
	RunMultiple ifTrue = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.runIfElse.ifTrue.name"));
	RunMultiple ifFalse = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.runIfElse.ifFalse.name"));
	public static final String conditionJsonKey = "condition";
	public static final String ifTrueJsonKey = "ifTrue";
	public static final String ifFalseJsonKey = "ifFalse";
	
	public RunIfElse(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @org.jetbrains.annotations.NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, ControlFlowIssue>
	compile(CompileEnvironment variableMap) {
		var compiledCondition = condition.compile(variableMap);
		var compiledIfTrue = ifTrue.compile(variableMap);
		var compiledIfFalse = ifFalse.compile(variableMap);
		return map->{
			var conditionResult = compiledCondition.scriptApply(map);
			if(conditionResult == null) throw ScriptRuntimeException.nullPointer(this);
			return (conditionResult ? compiledIfTrue : compiledIfFalse).scriptApply(map);
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.add(conditionJsonKey, condition.getAsJsonElement());
		res.add(ifTrueJsonKey, ifTrue.getAsJsonElement());
		res.add(ifFalseJsonKey, ifFalse.getAsJsonElement());
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig("runIfElse", element);
			return;
		}
		condition.setValueFromJsonElement(object.get(conditionJsonKey));
		ifTrue.setValueFromJsonElement(object.get(ifTrueJsonKey));
		ifFalse.setValueFromJsonElement(object.get(ifFalseJsonKey));
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {
		return List.of(condition, ifTrue, ifFalse);
	}
}
