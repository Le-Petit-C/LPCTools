package lpctools.script.suppliers;

import lpctools.script.CompileEnvironment;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplierInteger extends IScriptSupplierNotNull<Integer>{
	default @NotNull ScriptNotNullSupplier<Integer>
	compileNotNull(CompileEnvironment environment){return compileInteger(environment);}
	
	@NotNull ScriptIntegerSupplier compileInteger(CompileEnvironment environment);
}
