package lpctools.script.suppliers.Integer;

import lpctools.script.suppliers.IScriptSupplier;

public interface IIntegerSupplier extends IScriptSupplier<Integer> {
	@Override default Class<? extends Integer> getSuppliedClass(){return Integer.class;}
}
