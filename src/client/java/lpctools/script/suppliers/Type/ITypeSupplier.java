package lpctools.script.suppliers.Type;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import lpctools.script.suppliers.ScriptType;

public interface ITypeSupplier extends IScriptSupplierNotNull<ScriptType> {
	//Boolean是final的，所以不需要? extends Boolean
	@Override default Class<ScriptType> getSuppliedClass(){return ScriptType.class;}
}
