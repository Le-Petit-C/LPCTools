package lpctools.mixin.client.scriptMixins.propertiesMixin;

import lpctools.script.suppliers.BlockPropertyOperators;
import net.minecraft.state.property.BooleanProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BooleanProperty.class)
public class BooleanPropertyRegisterMixin {
	@Inject(method = "<init>", at = @At("RETURN") )
	private void onInit(String name, CallbackInfo ci){
		BlockPropertyOperators.BooleanPropertyGetter.propertyGetters.registerProperty((BooleanProperty)(Object)this);
	}
}
