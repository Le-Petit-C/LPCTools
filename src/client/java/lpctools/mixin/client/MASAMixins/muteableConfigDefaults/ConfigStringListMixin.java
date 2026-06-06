package lpctools.mixin.client.MASAMixins.muteableConfigDefaults;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ConfigStringList.class)
public class ConfigStringListMixin {
	@Shadow @Final @Mutable private ImmutableList<String> defaultValue;
}
