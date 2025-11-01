package lpctools.script.suppliers;

import lpctools.script.CompileTimeVariableMap;
import lpctools.script.IScript;
import lpctools.script.RuntimeVariableMap;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplier<T> extends IScript {
	@Override default Text getName(){
		return ScriptSupplierLake.getSupplierRegistration(this).displayName;
	}
	
	Class<? extends T> getSuppliedClass();
	@NotNull ScriptFunction<RuntimeVariableMap, T>
	compile(CompileTimeVariableMap variableMap);
}
