package lpctools.mixin.client.scriptMixins;

import lpctools.script.suppliers.TagKey.ConstantTagKey;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TagKey.class)
public class TagKeyMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	void mixinInitReturn(CallbackInfo ci){
		ConstantTagKey.addTagKey((TagKey<?>)(Object)this);
	}
}
