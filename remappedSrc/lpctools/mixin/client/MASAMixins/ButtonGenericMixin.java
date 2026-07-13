package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import lpctools.mixinInterfaces.MASAMixins.IButtonGenericMixin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ButtonGeneric.class, remap = false)
public abstract class ButtonGenericMixin implements IButtonGenericMixin {
	@Shadow(remap = false) @Final @Mutable @Nullable protected IGuiIcon icon;
	@Accessor(value = "icon", remap = false) @Override public abstract void setIcon(IGuiIcon icon);
}
