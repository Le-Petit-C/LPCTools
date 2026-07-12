package lpctools.script.suppliers.Entity.PlayerEntity;

import lpctools.script.suppliers.IScriptSupplier;
import net.minecraft.world.entity.player.Player;

public interface IPlayerEntitySupplier extends IScriptSupplier<Player> {
	@Override default Class<? extends Player> getSuppliedClass(){return Player.class;}
}
