package lpctools.mixin.client.accessors;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.malilib.config.options.ConfigStringList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ConfigStringList.class)
public interface ConfigStringListAccessor {
	@Accessor void setDefaultValue(ImmutableList<String> defaultValue);
}
