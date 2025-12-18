package lpctools.mixin.client.propertiesMixin;

import lpctools.script.suppliers.BlockStatePropertyGettersAsFunction;
import net.minecraft.state.property.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Property.class)
public class PropertyRegisterMixin {
	@Inject(method = "<init>", at = @org.spongepowered.asm.mixin.injection.At("RETURN") )
	private void onInit(String name, Class<?> type, CallbackInfo ci){
		BlockStatePropertyGettersAsFunction.DoHasProperty.propertyGetters.registerProperty((Property<?>)(Object)this);
	}
}
