package lpctools.script.suppliers.BlockProperty.IntegerBlockProperty.EnumBlockProperty;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.state.property.IntProperty;

public interface IIntegerBlockPropertySupplier extends IScriptSupplierNotNull<IntProperty> {
	@Override default Class<IntProperty> getSuppliedClass() {
		return IntProperty.class;
	}
}
