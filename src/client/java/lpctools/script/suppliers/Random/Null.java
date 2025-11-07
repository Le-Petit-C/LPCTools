package lpctools.script.suppliers.Random;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Null<T> extends AbstractScript implements IRandomSupplier<T> {
	public final Class<T> suppliedClass;
	public Null(IScriptWithSubScript parent, Class<T> suppliedClass) {
		super(parent);
		this.suppliedClass = suppliedClass;
	}
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, T> compile(CompileEnvironment variableMap) {return map->null;}
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Object> compileRandom(CompileEnvironment variableMap) {return map->null;}
	@Override public Class<? extends T> getSuppliedClass() {return suppliedClass;}
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
