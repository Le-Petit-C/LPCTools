package lpctools.script.suppliers.Random;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNullableSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Null<T> extends AbstractScript implements IRandomSupplier<T> {
	public final Class<T> suppliedClass;
	public Null(IScriptWithSubScript parent, Class<T> suppliedClass) {
		super(parent);
		this.suppliedClass = suppliedClass;
	}
	@Override public @NotNull ScriptNullableSupplier<T> compile(CompileEnvironment environment) {return map->null;}
	@Override public @NotNull ScriptNullableSupplier<Object> compileRandom(CompileEnvironment environment) {return map->null;}
	@Override public Class<? extends T> getSuppliedClass() {return suppliedClass;}
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
