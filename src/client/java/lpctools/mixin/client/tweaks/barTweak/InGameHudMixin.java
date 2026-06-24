package lpctools.mixin.client.tweaks.barTweak;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import lpctools.tweaks.BarTweaks;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.client.gui.contextualbar.ExperienceBar;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBar;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Hud.class)
public class InGameHudMixin {
    @Shadow @Final private Map<?, Supplier<ContextualBar>> contextualInfoBars;
    private ContextualBar findBar(Class<? extends ContextualBar> type) {
        for(Supplier<ContextualBar> supplier : contextualInfoBars.values()) {
            ContextualBar bar = supplier.get();
            if(type.isInstance(bar)) return bar;
        }
        return ContextualBar.EMPTY;
    }
    @ModifyExpressionValue(method = "extractHotbarAndDecorations",
        at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Pair;getSecond()Ljava/lang/Object;", ordinal = 0, remap = false))
    Object modifyValue0(Object original){
        if(BarTweaks.locatorBarUsesExpBackground.getAsBoolean() && original instanceof LocatorBar)
            return findBar(ExperienceBar.class);
        return original;
    }
    @ModifyExpressionValue(method = "extractHotbarAndDecorations",
        at = @At(value = "INVOKE", target = "Lcom/mojang/datafixers/util/Pair;getSecond()Ljava/lang/Object;", ordinal = 1, remap = false))
    Object modifyValue1(Object original){
        if(BarTweaks.expBarDisplaysLocatorPoints.getAsBoolean() && original instanceof ExperienceBar)
            return findBar(LocatorBar.class);
        if(BarTweaks.jumpBarDisplaysLocatorPoints.getAsBoolean() && original instanceof JumpableVehicleBar)
            return findBar(LocatorBar.class);
        return original;
    }
}
