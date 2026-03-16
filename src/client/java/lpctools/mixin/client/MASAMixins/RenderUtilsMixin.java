package lpctools.mixin.client.MASAMixins;

import com.llamalad7.mixinextras.sugar.Local;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(RenderUtils.class)
public class RenderUtilsMixin {
	@ModifyArgs(method = "drawHoverText", at = @At(value = "INVOKE", target = "Lfi/dy/masa/malilib/render/RenderUtils;drawGradientRect(FFFFFII)V"))
	private static void translationWeakFix(Args args, @Local(argsOnly = true) DrawContext drawContext) {
		var matrices = drawContext.getMatrices().peek();
		var positionMatrix = matrices.getPositionMatrix();
		float tx = positionMatrix.get(3, 0);
		float ty = positionMatrix.get(3, 1);
		args.set(0, (float)args.get(0) + tx);
		args.set(1, (float)args.get(1) + ty);
		args.set(2, (float)args.get(2) + tx);
		args.set(3, (float)args.get(3) + ty);
	}
}
