package lpctools.script.suppliers.ControlFlowIssue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithSubScriptMutable;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class RunMultiple extends AbstractSupplierWithSubScriptMutable<ControlFlowIssue, ControlFlowIssue> implements IControlFlowIssueSupplier {
	private @Nullable Iterable<?> widgets;
	protected final @Nullable Text name;
	
	public RunMultiple(IScriptWithSubScript parent, @Nullable Text name) {
		super(parent);
		this.name = name;
	}
	public RunMultiple(IScriptWithSubScript parent) {this(parent, null);}
	
	@Override public Class<ControlFlowIssue> getArgumentClass() {return ControlFlowIssue.class;}
	@Override public @NotNull ScriptNotNullSupplier<ControlFlowIssue>
	compileNotNull(CompileEnvironment environment) {
		ArrayList<ScriptNotNullSupplier<? extends ControlFlowIssue>> compiledSubRunners = new ArrayList<>();
		for(var sub : getSubScripts()) compiledSubRunners.add(sub.compileCheckedNotNull(environment));
		return map-> {
			for(var runnable : compiledSubRunners){
				var issue = runnable.scriptApply(map);
				if(issue.shouldEndRunMultiple) return issue;
			}
			return ControlFlowIssue.NO_ISSUE;
		};
	}
	@Override public @Nullable JsonElement getAsJsonElement() {return getSubSuppliersAsJsonArray();}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if(element instanceof JsonArray array) loadSubSuppliersFromJsonArray(array);
		else warnFailedLoadingConfig(getName(), element);
	}
	
	@Override public @Nullable Iterable<?> getWidgets() {
		if(widgets == null) widgets = List.of(createAddButton());
		return widgets;
	}
	@Override @Nullable public Text getName() {
		if(name != null) return name;
		else return super.getName();
	}
}
