package lpctools.script.suppliers.Random;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSignResultSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import static lpctools.script.suppliers.BlockStatePropertyGettersAsFunction.*;

public class BlockStateEnumProperty<T> extends AbstractSignResultSupplier<EnumPropertyGetter> implements IRandomSupplier<T> {
	protected final SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Text.translatable("lpctools.script.suppliers.random.blockStateEnumProperty.subSuppliers.blockState.name"), "blockState");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState);
	
	public BlockStateEnumProperty(IScriptWithSubScript parent, Class<T> suppliedClass) {
		super(parent, EnumPropertyGetter.propertyGetters.getFirstProperty(), EnumPropertyGetter.propertyGetters, 1);
		this.suppliedClass = suppliedClass;
	}
	
	public final Class<T> suppliedClass;
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<Object>
	compileRandom(CompileEnvironment environment) {
		var sign = compareSign;
		var blockStateSupplier = blockState.get().compileCheckedNotNull(environment);
		return map->{
			try{
				return sign.getEnum(blockStateSupplier.scriptApply(map));
			} catch (IllegalArgumentException e){
				throw ScriptRuntimeException.illegalArgument(this, e.getMessage());
			}
		};
	}
	
	@Override public Class<T> getSuppliedClass() {return suppliedClass;}
}
