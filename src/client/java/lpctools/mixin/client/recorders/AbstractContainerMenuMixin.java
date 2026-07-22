package lpctools.mixin.client.recorders;

import lpctools.mixinData.AbstractContainerMenuExtraData;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin implements AbstractContainerMenuExtraData.Getter {
	@Shadow @Final public NonNullList<Slot> slots;

	@Unique final AbstractContainerMenuExtraData.Mutable data = new AbstractContainerMenuExtraData.Mutable((AbstractContainerMenu)(Object)this);

	@Override public AbstractContainerMenuExtraData lpctools$getAbstractContainerMenuExtraData() { return data; }

	@Inject(method = "addInventoryHotbarSlots", at = @At("HEAD"))
	void injectAddInventoryHotbarSlotsHead(Container inventory, int left, int top, CallbackInfo ci) {
		data.setHotbarStartIndex(slots.size());
	}
}
