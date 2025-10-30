package lpctools.script.suppliers.voids;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileTimeVariableMap;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.RuntimeVariableMap;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoNothing extends AbstractScript implements IVoidSupplier {
	public DoNothing(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptFunction<RuntimeVariableMap, Void>
	compile(CompileTimeVariableMap variableMap) {return map->null;}
	@Override public @Nullable String getName() {
		return Text.translatable("lpctools.script.suppliers.voids.doNothing.name").getString();
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
