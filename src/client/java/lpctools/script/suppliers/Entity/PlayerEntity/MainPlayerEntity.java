package lpctools.script.suppliers.Entity.PlayerEntity;

import com.google.gson.JsonElement;
import lpctools.script.*;
import lpctools.script.runtimeInterfaces.ScriptNullableSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainPlayerEntity extends AbstractScript implements IPlayerEntitySupplier {
	public MainPlayerEntity(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptNullableSupplier<Player>
	compile(CompileEnvironment environment) {return map->Minecraft.getInstance().player;}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
