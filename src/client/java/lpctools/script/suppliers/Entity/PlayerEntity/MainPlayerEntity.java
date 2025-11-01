package lpctools.script.suppliers.Entity.PlayerEntity;

import com.google.gson.JsonElement;
import lpctools.script.*;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainPlayerEntity extends AbstractScript implements IPlayerEntitySupplier {
	public MainPlayerEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, PlayerEntity>
	compile(CompileEnvironment variableMap) {
		return map->MinecraftClient.getInstance().player;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
