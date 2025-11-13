package lpctools.script.suppliers.String;

import lpctools.script.suppliers.IScriptSupplierNotNull;

public interface IStringSupplier extends IScriptSupplierNotNull<String> {
	@Override default Class<? extends String> getSuppliedClass(){return String.class;}
}
