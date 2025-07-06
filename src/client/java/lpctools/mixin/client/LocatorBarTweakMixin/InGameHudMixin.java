package lpctools.mixin.client.LocatorBarTweakMixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lpctools.tweaks.LocatorBarTweak;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.gui.hud.bar.ExperienceBar;
import net.minecraft.client.gui.hud.bar.JumpBar;
import net.minecraft.client.gui.hud.bar.LocatorBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private Map<InGameHud.BarType, Supplier<Bar>> bars;
    @ModifyExpressionValue(method = "renderMainHud", remap = false,
        at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getValue()Ljava/lang/Object;", ordinal = 0))
    Object modifyValue0(Object original){
        if(LocatorBarTweak.locatorBarUsesExpBackground.getAsBoolean() && original instanceof LocatorBar)
            return bars.get(InGameHud.BarType.EXPERIENCE).get();
        return original;
    }
    @ModifyExpressionValue(method = "renderMainHud", remap = false,
        at = @At(value = "INVOKE", target = "Lorg/apache/commons/lang3/tuple/Pair;getValue()Ljava/lang/Object;", ordinal = 1))
    Object modifyValue1(Object original){
        if(LocatorBarTweak.expBarDisplaysLocatorPoints.getAsBoolean() && original instanceof ExperienceBar)
            return bars.get(InGameHud.BarType.LOCATOR).get();
        if(LocatorBarTweak.jumpBarDisplaysLocatorPoints.getAsBoolean() && original instanceof JumpBar)
            return bars.get(InGameHud.BarType.LOCATOR).get();
        return original;
    }
}
