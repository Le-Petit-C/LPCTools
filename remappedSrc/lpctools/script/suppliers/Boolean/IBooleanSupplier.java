package lpctools.script.suppliers.Boolean;

import lpctools.script.suppliers.IScriptSupplierBoolean;

public interface IBooleanSupplier extends IScriptSupplierBoolean {
	//Boolean是final的，所以不需要? extends Boolean
	@Override default Class<Boolean> getSuppliedClass(){return Boolean.class;}
}
