package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.script.AbstractScriptWithSubScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.Boolean.And;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class WhileLoop extends AbstractScriptWithSubScript implements IControlFlowIssueSupplier {
	public final And condition = new And(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.whileLoop.condition.name"));
	public final RunMultiple loopBody = new RunMultiple(this, Text.translatable("lpctools.script.suppliers.ControlFlowIssue.whileLoop.loopBody.name"));
	public static final String conditionJsonKey = "condition";
	public static final String loopBodyJsonKey = "loopBody";
	
	public WhileLoop(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		var compiledCondition = condition.compileCheckedNotNull(environment);
		var compiled = loopBody.compileCheckedNotNull(environment);
		return map->{
			while (compiledCondition.scriptApply(map)) {
				var flow = compiled.scriptApply(map);
				if (flow.shouldBreak) return flow.applied();
			}
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		JsonObject res = new JsonObject();
		res.add(conditionJsonKey, condition.getAsJsonElement());
		res.add(loopBodyJsonKey, loopBody.getAsJsonElement());
		return res;
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(!(element instanceof JsonObject object)){
			warnFailedLoadingConfig("runIfElse", element);
			return;
		}
		condition.setValueFromJsonElement(object.get(conditionJsonKey));
		loopBody.setValueFromJsonElement(object.get(loopBodyJsonKey));
	}
	
	@Override public @NotNull List<? extends IScript> getSubScripts() {
		return List.of(condition, loopBody);
	}
}
