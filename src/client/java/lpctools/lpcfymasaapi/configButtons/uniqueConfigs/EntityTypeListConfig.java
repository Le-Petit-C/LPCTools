package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityTypeListConfig extends MutableConfig<EntityTypeConfig>{
	private @NotNull ImmutableSet<EntityType<?>> entityTypes = ImmutableSet.of();
	public EntityTypeListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull String buttonKeyPrefix, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, buttonKeyPrefix, ImmutableMap.of("entity", (config, key)->new EntityTypeConfig(config, key, EntityType.PLAYER, config::onValueChanged)), callback);
	}
	@Override public void onValueChanged() {
		ImmutableSet.Builder<EntityType<?>> builder = ImmutableSet.builder();
		for(EntityTypeConfig config : iterateConfigs())
			builder.add(config.getEntityType());
		entityTypes = builder.build();
		super.onValueChanged();
	}
	@SuppressWarnings("unused")
	public Iterable<EntityType<?>> iterateEntities(){return entityTypes;}
	public boolean contains(EntityType<?> type){return entityTypes.contains(type);}
}
