package lpctools.script.suppliers.BlockProperty.BooleanBlockProperty.EnumBlockProperty;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public interface IBooleanBlockPropertySupplier extends IScriptSupplierNotNull<BooleanProperty> {
	@Override default Class<BooleanProperty> getSuppliedClass() {
		return BooleanProperty.class;
	}
}
