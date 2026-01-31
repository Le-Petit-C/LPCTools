package lpctools.script.suppliers.Enum;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class BlockStateEnumProperty extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IEnumSupplier {
	protected final SupplierStorage<BlockState> blockState = ofStorage(BlockState.class,
		Text.translatable("lpctools.script.suppliers.enum.blockStateEnumProperty.subSuppliers.blockState.name"), "blockState");
	@SuppressWarnings("rawtypes")
	protected final SupplierStorage<EnumProperty> property = ofStorage(EnumProperty.class,
		Text.translatable("lpctools.script.suppliers.enum.blockStateEnumProperty.subSuppliers.property.name"), "property");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(blockState, property);
	
	public BlockStateEnumProperty(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@SuppressWarnings("rawtypes")
	@Override public @NotNull ScriptNotNullSupplier<Enum>
	compileNotNull(CompileEnvironment environment) {
		var blockStateSupplier = blockState.get().compileCheckedNotNull(environment);
		var propertySupplier = property.get().compileCheckedNotNull(environment);
		return map -> {
			try {
				//由于使用的是RawType，编译器无法从EnumProperty<E>中推断出E的类型，需要显式指定其为Enum，所以这里使用了“冗余的类型参数”（其实并不冗余）
				//noinspection unchecked,RedundantTypeArguments
				return blockStateSupplier.scriptApply(map).<Enum>get(propertySupplier.scriptApply(map));
			} catch (IllegalArgumentException e) {
				throw ScriptRuntimeException.illegalArgument(this, e.getMessage());
			}
		};
	}
}
