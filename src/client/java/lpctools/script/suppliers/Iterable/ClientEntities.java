package lpctools.script.suppliers.Iterable;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClientEntities extends AbstractScript implements IIterableSupplier {
	public ClientEntities(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, ObjectIterable>
	compile(CompileEnvironment variableMap) {
		return map->{
			var world = MinecraftClient.getInstance().world;
			if (world != null) return ObjectIterable.of(world.getEntities());
			else return ObjectIterable.empty;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
