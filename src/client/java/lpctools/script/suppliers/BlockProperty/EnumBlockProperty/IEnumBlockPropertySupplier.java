package lpctools.script.suppliers.BlockProperty.EnumBlockProperty;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.world.level.block.state.properties.EnumProperty;

@SuppressWarnings("rawtypes")
public interface IEnumBlockPropertySupplier extends IScriptSupplierNotNull<EnumProperty> {
	@Override default Class<EnumProperty> getSuppliedClass() {
		return EnumProperty.class;
	}
}
