package lpctools.script.suppliers.TagKey;

import lpctools.script.suppliers.IScriptSupplierNotNull;
import net.minecraft.registry.tag.TagKey;

import java.util.HashSet;

@SuppressWarnings("rawtypes")
public interface ITagKeySupplier extends IScriptSupplierNotNull<TagKey> {
	HashSet<TagKey<?>> allTagKeys = new HashSet<>();
	@Override default Class<? extends TagKey> getSuppliedClass() {
		return TagKey.class;
	}
}
