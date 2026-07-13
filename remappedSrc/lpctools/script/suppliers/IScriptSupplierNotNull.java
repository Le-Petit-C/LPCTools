package lpctools.script.suppliers;

import lpctools.script.CompileEnvironment;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.runtimeInterfaces.ScriptNullableSupplier;
import org.jetbrains.annotations.NotNull;

public interface IScriptSupplierNotNull<T> extends IScriptSupplier<T> {
	@Deprecated default @NotNull ScriptNullableSupplier<T>
	compile(CompileEnvironment environment){return compileNotNull(environment);}
	@NotNull ScriptNotNullSupplier<T> compileNotNull(CompileEnvironment environment);
}
