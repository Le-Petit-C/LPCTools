package lpctools.script.suppliers.BlockProperty.IntegerBlockProperty.EnumBlockProperty;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface IIntegerBlockPropertySupplier extends IScriptSupplierNotNull<IntegerProperty> {
	@Override default Class<IntegerProperty> getSuppliedClass() {
		return IntegerProperty.class;
	}
}
