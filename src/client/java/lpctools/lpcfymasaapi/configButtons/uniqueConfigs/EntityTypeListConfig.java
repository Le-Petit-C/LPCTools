package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EntityTypeListConfig extends ConfigListConfig<EntityTypeConfig>{
	protected @NotNull ImmutableSet<EntityType<?>> entityTypes = ImmutableSet.of();
	private boolean suppressValueChanged = false;
	public EntityTypeListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, config->new EntityTypeConfig(config, "entity", EntityType.PLAYER, config::onValueChanged), callback);
	}
	
	public boolean contains(EntityType<?> type){return entityTypes.contains(type);}
	public @NotNull ImmutableSet<EntityType<?>> getEntityTypes(){return entityTypes;}
	
	public void setEntityTypes(Iterable<? extends EntityType<?>> entityTypes) {
		suppressValueChanged = true;
		subConfigs.clear();
		for(var entityType : entityTypes) allocateAndAddConfig().setEntityType(entityType);
		suppressValueChanged = false;
		onValueChanged();
	}
	
	@Override public void onValueChanged() {
		if(suppressValueChanged) return;
		ImmutableSet.Builder<EntityType<?>> builder = ImmutableSet.builder();
		for(EntityTypeConfig config : iterateConfigs())
			builder.add(config.getEntityType());
		entityTypes = builder.build();
		super.onValueChanged();
	}
}
