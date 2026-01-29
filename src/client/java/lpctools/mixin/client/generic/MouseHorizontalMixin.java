package lpctools.mixin.client.generic;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static lpctools.generic.GenericConfigs.horizontalScrollButton;

@Mixin(Mouse.class)
public class MouseHorizontalMixin {
	@Unique private double lpctools$originalVertical;
	
	@Unique private static boolean isHorizontalScrollKeyDown(){
		return horizontalScrollButton.getKeybind().isPressed();
	}
	
	@ModifyVariable(
		method = "onMouseScroll",
		at = @At("HEAD"),
		index = 5,
		argsOnly = true,
		order = 901
	)
	private double captureVertical(double vertical) {
		lpctools$originalVertical = vertical;
		return vertical;
	}
	
	@ModifyVariable(
		method = "onMouseScroll",
		at = @At("HEAD"),
		index = 3,
		argsOnly = true,
		order = 902
	)
	private double modifyHorizontal(double horizontal) {
		if (isHorizontalScrollKeyDown())
			return horizontal - lpctools$originalVertical;
		return horizontal;
	}
	
	@ModifyVariable(
		method = "onMouseScroll",
		at = @At("HEAD"),
		index = 5,
		argsOnly = true,
		order = 903
	)
	private double modifyVertical(double vertical) {
		return isHorizontalScrollKeyDown() ? 0.0 : vertical;
	}
}
