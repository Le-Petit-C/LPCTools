package lpctools.mixin.client.generic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static lpctools.generic.GenericConfigs.horizontalScrollButton;

import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public class MouseHorizontalMixin {
	@Unique private double lpctools$originalYOffset;
	
	@Unique private static boolean isHorizontalScrollKeyDown(){
		return horizontalScrollButton.getKeybind().isPressed();
	}
	
	@ModifyVariable(
		method = "onScroll",
		at = @At("HEAD"),
		index = 5,
		argsOnly = true,
		order = 901)
	private double captureYOffset(double yoffset) {
		lpctools$originalYOffset = yoffset;
		return yoffset;
	}
	
	@ModifyVariable(
		method = "onScroll",
		at = @At("HEAD"),
		index = 3,
		argsOnly = true,
		order = 902)
	private double modifyXOffset(double xoffset) {
		if (isHorizontalScrollKeyDown())
			return xoffset - lpctools$originalYOffset;
		return xoffset;
	}
	
	@ModifyVariable(
		method = "onScroll",
		at = @At("HEAD"),
		index = 5,
		argsOnly = true,
		order = 903)
	private double modifyYOffset(double yoffset) {
		return isHorizontalScrollKeyDown() ? 0.0 : yoffset;
	}
}
