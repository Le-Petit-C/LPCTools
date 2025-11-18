package lpctools.script.suppliers.Integer;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScreenSlotCount extends AbstractScript implements IIntegerSupplier {
	public ScreenSlotCount(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		return map -> {
			var player = MinecraftClient.getInstance().player;
			if(player != null) return player.currentScreenHandler.slots.size();
			else return 0;
		};
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
