package lpctools.mixin.client.EntityHighlight;

import lpctools.tools.entityHighlight.EntityHighlight;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public abstract EntityType<?> getType();
	@Inject(method = "isGlowing", at = @At("RETURN"), cancellable = true)
	void isGlowing(CallbackInfoReturnable<Boolean> cir){
		if(EntityHighlight.EHConfig.getBooleanValue() && EntityHighlight.entityList.contains(getType()))
			cir.setReturnValue(true);
	}
}
