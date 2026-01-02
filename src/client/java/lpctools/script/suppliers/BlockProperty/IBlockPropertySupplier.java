package lpctools.script.suppliers.BlockProperty;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.state.property.Property;

@SuppressWarnings("rawtypes")
public interface IBlockPropertySupplier extends IScriptSupplierNotNull<Property> {
	@Override default Class<? extends Property> getSuppliedClass() {
		return Property.class;
	}
}

// 克服了raw-types恐惧症qwq
// 不过raw-types还是能少用点就少用点吧
