package lpctools.mixin.client.propertiesMixin;

import lpctools.script.suppliers.BlockStatePropertyGettersAsFunction;
import net.minecraft.state.property.IntProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IntProperty.class)
public class IntegerPropertyRegisterMixin {
	@Inject(method = "<init>", at = @org.spongepowered.asm.mixin.injection.At("RETURN") )
	private void onInit(String name, int min, int max, CallbackInfo ci){
		BlockStatePropertyGettersAsFunction.IntegerPropertyGetter.propertyGetters.registerProperty((IntProperty)(Object)this);
	}
}
