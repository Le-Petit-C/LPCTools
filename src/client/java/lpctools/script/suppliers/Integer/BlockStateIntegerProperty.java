package lpctools.script.suppliers.Integer;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptIntegerSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static lpctools.script.suppliers.BlockStatePropertyGettersAsFunction.*;

public class BlockStateIntegerProperty extends AbstractSignResultSupplier<IntegerPropertyGetter> implements IIntegerSupplier {
	protected final SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Text.translatable("lpctools.script.suppliers.integer.blockStateIntegerProperty.subSuppliers.blockState.name"), "blockState");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState);
	
	public BlockStateIntegerProperty(IScriptWithSubScript parent) {
		super(parent, IntegerPropertyGetter.propertyGetters.getFirstProperty(), IntegerPropertyGetter.propertyGetters, 1);
	}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptIntegerSupplier
	compileInteger(CompileEnvironment environment) {
		var sign = compareSign;
		var blockStateSupplier = blockState.get().compileCheckedNotNull(environment);
		return map->{
			try{
				return sign.getInteger(blockStateSupplier.scriptApply(map));
			} catch (IllegalArgumentException e){
				throw ScriptRuntimeException.illegalArgument(this, e.getMessage());
			}
		};
	}
}
