package lpctools.script.suppliers;

import lpctools.script.CompileEnvironment;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplierBoolean extends IScriptSupplierNotNull<Boolean>{
	default @NotNull ScriptNotNullSupplier<Boolean>
	compileNotNull(CompileEnvironment environment){return compileBoolean(environment);}
	
	@NotNull ScriptBooleanSupplier compileBoolean(CompileEnvironment environment);
}
