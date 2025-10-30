package lpctools.script.suppliers.voids;

import lpctools.script.suppliers.IScriptSupplier;

public interface IVoidSupplier extends IScriptSupplier<Void> {
	@Override default Class<Void> getSuppliedClass(){return Void.class;}
}
