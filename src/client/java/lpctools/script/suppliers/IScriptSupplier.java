package lpctools.script.suppliers;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullFunction;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplier<T> extends IScript {
	@Override default Text getName(){
		return ScriptSupplierLake.getSupplierRegistration(this).displayName;
	}
	
	Class<? extends T> getSuppliedClass();
	@NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, T>
	compile(CompileEnvironment variableMap);
	
	@NotNull default ScriptNotNullFunction<CompileEnvironment.RuntimeVariableMap, T>
	compileCheckedNotNull(CompileEnvironment variableMap){
		if(this instanceof IScriptSupplierNotNull<T> scriptSupplierNotNull)
			return scriptSupplierNotNull.compileNotNull(variableMap);
		else return IScriptSupplierNotNull.ofNotNull(this, compile(variableMap));
	}
}
