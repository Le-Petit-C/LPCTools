package lpctools.mixin.client.tweaks.barTweak;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lpctools.tweaks.BarTweaks;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Gui.class)
public class InGameHudMixin {
    @Shadow @Final private Map<Gui.ContextualInfo, Supplier<ContextualBarRenderer>> contextualInfoBarRenderers;
    @ModifyExpressionValue(method = "renderHotbarAndDecorations",
        at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getValue()Ljava/lang/Object;", ordinal = 0, remap = false))
    Object modifyValue0(Object original){
        if(BarTweaks.locatorBarUsesExpBackground.getAsBoolean() && original instanceof LocatorBarRenderer)
            return contextualInfoBarRenderers.get(Gui.ContextualInfo.EXPERIENCE).get();
        return original;
    }
    @ModifyExpressionValue(method = "renderHotbarAndDecorations",
        at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getValue()Ljava/lang/Object;", ordinal = 1, remap = false))
    Object modifyValue1(Object original){
        if(BarTweaks.expBarDisplaysLocatorPoints.getAsBoolean() && original instanceof ExperienceBarRenderer)
            return contextualInfoBarRenderers.get(Gui.ContextualInfo.LOCATOR).get();
        if(BarTweaks.jumpBarDisplaysLocatorPoints.getAsBoolean() && original instanceof JumpableVehicleBarRenderer)
            return contextualInfoBarRenderers.get(Gui.ContextualInfo.LOCATOR).get();
        return original;
    }
}
