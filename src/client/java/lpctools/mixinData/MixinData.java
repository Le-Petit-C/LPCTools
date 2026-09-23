package lpctools.mixinData;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MixinData {
	public static AbstractContainerMenuExtraData getData(AbstractContainerMenu mixinTarget)
	{ return ((AbstractContainerMenuExtraData.Getter)mixinTarget).lpctools$getAbstractContainerMenuExtraData(); }
	public static MultiPlayerGameModeExtraData getData(MultiPlayerGameMode mixinTarget)
	{ return ((MultiPlayerGameModeExtraData.Getter)mixinTarget).lpctools$getMultiPlayerGameModeExtraData(); }
}
