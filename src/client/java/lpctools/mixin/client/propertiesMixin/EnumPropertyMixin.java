package lpctools.mixin.client.propertiesMixin;

import lpctools.script.suppliers.BlockStatePropertyGettersAsFunction;
import lpctools.script.suppliers.Random.ConstantEnum;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnumProperty.class)
public class EnumPropertyMixin<T extends Enum<T> & StringIdentifiable> {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(String name, Class<T> type, List<T> values, CallbackInfo ci) {
		ConstantEnum.enumInfo.registerEnum(type);
		BlockStatePropertyGettersAsFunction.EnumPropertyGetter.propertyGetters.registerProperty((EnumProperty<?>) (Object) this);
	}
}
