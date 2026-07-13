package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.gui.button.ConfigButtonKeybind;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import lpctools.mixinInterfaces.MASAMixins.IConfigButtonKeybindMixin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ConfigButtonKeybind.class, remap = false)
public abstract class ConfigButtonKeybindMixin implements IConfigButtonKeybindMixin {
	@Shadow @Final @Mutable @Nullable
	protected IKeybindConfigGui host;
	@Accessor(value = "host", remap = false)
	public abstract void setHost(@Nullable IKeybindConfigGui host);
}
