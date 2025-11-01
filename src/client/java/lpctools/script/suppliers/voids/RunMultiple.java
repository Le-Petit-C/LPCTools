package lpctools.script.suppliers.voids;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.RuntimeVariableMap;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithSubScriptMutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class RunMultiple extends AbstractSupplierWithSubScriptMutable<Void, Void> implements IVoidSupplier {
	private @Nullable Iterable<?> widgets;
	
	public RunMultiple(IScriptWithSubScript parent) {super(parent);}
	
	@Override public Class<Void> getArgumentClass() {return Void.class;}
	@Override public @NotNull ScriptFunction<RuntimeVariableMap, Void>
	compile(CompileEnvironment variableMap) {
		ArrayList<ScriptFunction<RuntimeVariableMap, ? extends Void>> compiledSubRunners = new ArrayList<>();
		for(var sub : getSubScripts()) compiledSubRunners.add(sub.compile(variableMap));
		return map-> {
			for(var runnable : compiledSubRunners)
				runnable.scriptApply(map);
			return null;
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
}
