package lpctools.script.suppliers.entities;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.entity.Entity;

public interface IEntitySupplier extends IScriptSupplier<Entity> {
	@Override default Class<? extends Entity> getSuppliedClass(){return Entity.class;}
}
