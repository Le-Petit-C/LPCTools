package lpctools.script.suppliers.Iterable;

import lpctools.script.suppliers.IScriptSupplierNotNull;

public interface IIterableSupplier extends IScriptSupplierNotNull<ObjectIterable> {
	@Override default Class<? extends ObjectIterable> getSuppliedClass(){return ObjectIterable.class;}
}
