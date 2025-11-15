package lpctools.script.suppliers.ItemStack;

import com.google.gson.JsonElement;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EmptyStack extends AbstractScript implements IItemStackSupplier {
	public EmptyStack(IScriptWithSubScript parent) {super(parent);}
	
	@Override public @NotNull ScriptNotNullSupplier<ItemStack>
	compileNotNull(CompileEnvironment environment) {return map->ItemStack.EMPTY;}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return null;}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {}
}
