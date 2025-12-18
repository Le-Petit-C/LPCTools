package lpctools.script.suppliers.Boolean;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptBooleanSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static lpctools.script.suppliers.BlockStatePropertyGettersAsFunction.*;

public class DoBlockStateHasProperty extends AbstractSignResultSupplier<DoHasProperty> implements IBooleanSupplier {
	protected final SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Text.translatable("lpctools.script.suppliers.boolean.doBlockStateHasProperty.subSuppliers.blockState.name"), "blockState");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState);
	
	public DoBlockStateHasProperty(IScriptWithSubScript parent) {
		super(parent, DoHasProperty.propertyGetters.getFirstProperty(), DoHasProperty.propertyGetters, 1);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptBooleanSupplier
	compileBoolean(CompileEnvironment environment) {
		var sign = compareSign;
		var blockStateSupplier = blockState.get().compileCheckedNotNull(environment);
		return map->sign.hasProperty(blockStateSupplier.scriptApply(map));
	}
}
