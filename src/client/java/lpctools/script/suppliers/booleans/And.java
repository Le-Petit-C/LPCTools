package lpctools.script.suppliers.booleans;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.script.suppliers.AbstractSupplierWithSubScriptMutable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class And extends AbstractSupplierWithSubScriptMutable<Boolean, Boolean> implements IBooleanSupplier {
	private @Nullable Iterable<?> widgets;
	
	public And(IScriptWithSubScript parent) {super(parent);}
	
	@Override public Class<Boolean> getArgumentClass() {return Boolean.class;}
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Boolean>
	compile(CompileEnvironment variableMap) {
		ArrayList<ScriptFunction<CompileEnvironment.RuntimeVariableMap, ? extends Boolean>> compiledSubRunners = new ArrayList<>();
		for(var sub : getSubScripts()) compiledSubRunners.add(sub.compile(variableMap));
		return map->{
			for(var runnable : compiledSubRunners){
				Boolean val = runnable.scriptApply(map);
				if(val == null) throw ScriptRuntimeException.nullPointer(this);
				if(!val) return false;
			}
			return true;
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
