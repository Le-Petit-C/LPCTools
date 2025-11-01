package lpctools.script.suppliers.voids;

import com.google.gson.JsonElement;
import lpctools.mixin.client.BlockReplaceAction;
import lpctools.script.AbstractScript;
import lpctools.script.CompileTimeVariableMap;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.RuntimeVariableMap;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DoAttack extends AbstractScript implements IVoidSupplier {
	public DoAttack(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptFunction<RuntimeVariableMap, Void>
	compile(CompileTimeVariableMap variableMap) {
		return map->{
			((BlockReplaceAction) MinecraftClient.getInstance()).invokeDoAttack();
			return null;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
