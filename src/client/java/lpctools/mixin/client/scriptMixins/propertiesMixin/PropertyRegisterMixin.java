package lpctools.mixin.client.scriptMixins.propertiesMixin;

import lpctools.script.suppliers.BlockPropertyOperators;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Property.class)
public class PropertyRegisterMixin {
	@Inject(method = "<init>", at = @org.spongepowered.asm.mixin.injection.At("RETURN") )
	private void onInit(String name, Class<?> type, CallbackInfo ci){
		BlockPropertyOperators.GenericPropertyOperator.propertyGetters.registerProperty((Property<?>)(Object)this);
	}
}
