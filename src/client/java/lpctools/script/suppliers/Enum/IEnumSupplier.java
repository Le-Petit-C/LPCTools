package lpctools.script.suppliers.Enum;

import lpctools.script.suppliers.IScriptSupplierNotNull;

@SuppressWarnings("rawtypes")
public interface IEnumSupplier extends IScriptSupplierNotNull<Enum> {
	@Override default Class<? extends Enum> getSuppliedClass() {
		return Enum.class;
	}
}

// 克服了raw-types恐惧症qwq
// 不过raw-types还是能少用点就少用点吧
