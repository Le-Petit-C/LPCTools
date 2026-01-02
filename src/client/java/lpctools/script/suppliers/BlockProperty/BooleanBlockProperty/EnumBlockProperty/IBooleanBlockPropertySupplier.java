package lpctools.script.suppliers.BlockProperty.BooleanBlockProperty.EnumBlockProperty;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.state.property.BooleanProperty;

public interface IBooleanBlockPropertySupplier extends IScriptSupplierNotNull<BooleanProperty> {
	@Override default Class<BooleanProperty> getSuppliedClass() {
		return BooleanProperty.class;
	}
}
