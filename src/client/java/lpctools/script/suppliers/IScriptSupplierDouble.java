package lpctools.script.suppliers;

import lpctools.script.CompileEnvironment;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.runtimeInterfaces.ScriptDoubleSupplier;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplierDouble extends IScriptSupplierNotNull<Double>{
	default @NotNull ScriptNotNullSupplier<Double>
	compileNotNull(CompileEnvironment environment){return compileDouble(environment);}
	
	@NotNull ScriptDoubleSupplier compileDouble(CompileEnvironment environment);
}
