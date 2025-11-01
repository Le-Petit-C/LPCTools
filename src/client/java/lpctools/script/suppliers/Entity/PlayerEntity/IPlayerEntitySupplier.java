package lpctools.script.suppliers.Entity.PlayerEntity;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.entity.player.PlayerEntity;

public interface IPlayerEntitySupplier extends IScriptSupplier<PlayerEntity> {
	@Override default Class<? extends PlayerEntity> getSuppliedClass(){return PlayerEntity.class;}
}
