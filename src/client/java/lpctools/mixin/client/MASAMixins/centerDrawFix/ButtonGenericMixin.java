package lpctools.mixin.client.MASAMixins.centerDrawFix;

import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.interfaces.IGuiIcon;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ButtonGeneric.class)
public abstract class ButtonGenericMixin extends ButtonBase {
	@Shadow(remap = false) protected LeftRight alignment;
	@Shadow(remap = false) @Final @Nullable protected IGuiIcon icon;
	
	public ButtonGenericMixin(int x, int y, int width, int height) {super(x, y, width, height);}
	@ModifyArg(method = "render", index = 1, at = @At(value = "INVOKE",
		target = "Lfi/dy/masa/malilib/render/RenderUtils;drawTexturedRect(Lnet/minecraft/util/Identifier;IIIIIILnet/minecraft/client/gui/DrawContext;)V"))
	int onDrawTexturedRect(int value){
		if(icon != null && alignment == LeftRight.CENTER)
			return x + (width - icon.getWidth()) / 2;
		else return value;
	}
}
