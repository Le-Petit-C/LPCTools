package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.screen.ChooseScreen;
import lpctools.util.DataUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class EntityTypeConfig extends UniqueStringConfig{
	@NotNull public EntityType<?> entity;
	@NotNull public final EntityType<?> defaultEntity;
	public EntityTypeConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull EntityType<?> defaultEntity, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, DataUtils.getEntityTypeId(defaultEntity), callback);
		entity = this.defaultEntity = defaultEntity;
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(ButtonWeightType.HEIGHT, 1, (button, mouseButton) -> choose(this::setEntityType), ()->"R", buttonGenericAllocator);
	}
	public void setEntityType(EntityType<?> entity){
		if(!this.entity.equals(entity)){
			this.entity = entity;
			stringValue = Registries.ENTITY_TYPE.getId(entity).toString();
			onValueChanged();
		}
	}
	public EntityType<?> getEntityType(){return entity;}
	@SuppressWarnings("unused")
	public EntityType<?> getDefaultEntityType(){return defaultEntity;}
	private static void choose(Consumer<EntityType<?>> consumer){
		HashMap<String, ChooseScreen.OptionCallback<Consumer<EntityType<?>>>> map = new LinkedHashMap<>();
		LinkedHashMap<String, String> tree = new LinkedHashMap<>();
		for(EntityType<?> entityType : Registries.ENTITY_TYPE){
			map.put(entityType.getTranslationKey(), (button, mouseButton, userData)->userData.accept(entityType));
			tree.put(entityType.getTranslationKey(), entityType.getTranslationKey());
		}
		ChooseScreen.openChooseScreen(
			Text.translatable("lpctools.configs.utils.entityConfig.chooseTitle").getString(),
			true, true, map, tree, consumer
		);
	}
	@Override public void setValueFromString(String s) {
		Identifier id = Identifier.tryParse(s);
		if(id != null){
			Identifier defId = Registries.ENTITY_TYPE.getDefaultId();
			EntityType<?> defEntity = Registries.ENTITY_TYPE.get(defId);
			EntityType<?> newEntity = Registries.ENTITY_TYPE.get(id);
			if(!defEntity.equals(newEntity) || defId.equals(id)){
				entity = newEntity;
				super.setValueFromString(s);
			}
		}
		getPage().markNeedUpdate();
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		 UpdateTodo todo = super.setValueFromJsonElementEx(element);
		 Identifier id = Identifier.tryParse(stringValue);
		 if(id != null) entity = Registries.ENTITY_TYPE.get(id);
		 return todo;
	}
}
