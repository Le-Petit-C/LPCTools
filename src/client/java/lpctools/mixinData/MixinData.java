package lpctools.mixinData;

import net.minecraft.world.inventory.AbstractContainerMenu;

public class MixinData {
	public static AbstractContainerMenuExtraData getData(AbstractContainerMenu mixinTarget)
	{ return ((AbstractContainerMenuExtraData.Getter)mixinTarget).lpctools$getAbstractContainerMenuExtraData(); }
}
