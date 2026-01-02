package lpctools.script.suppliers.BlockProperty.EnumBlockProperty;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.state.property.EnumProperty;

@SuppressWarnings("rawtypes")
public interface IEnumBlockPropertySupplier extends IScriptSupplierNotNull<EnumProperty> {
	@Override default Class<EnumProperty> getSuppliedClass() {
		return EnumProperty.class;
	}
}
