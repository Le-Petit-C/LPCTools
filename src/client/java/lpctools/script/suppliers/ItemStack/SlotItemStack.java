package lpctools.script.suppliers.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Entity.PlayerEntity.MainPlayerEntity;
import lpctools.script.utils.StackGetter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class SlotItemStack extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IItemStackSupplier{
	protected final SupplierStorage<Entity> entity = ofStorage(Entity.class, new MainPlayerEntity(this),
		Text.translatable("lpctools.script.suppliers.ItemStack.slotItemStack.subSuppliers.entity.name"), "entity");
	protected @NotNull StackGetter getter = StackGetter.values()[0];
	protected @Nullable WidthAutoAdjustButtonGeneric cycleButton;
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(entity);
	public static final String getterJsonKey = "getter";
	
	public SlotItemStack(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override protected ArrayList<Object> buildWidgets(ArrayList<Object> res) {
		super.buildWidgets(res);
		if(cycleButton == null) {
			cycleButton = new WidthAutoAdjustButtonGeneric(getDisplayWidget(), 0, 0, 20, getter.name.getString(), null);
			cycleButton.setActionListener((button, mouseButton)->getter = getter.cycle(mouseButton == 0));
		}
		res.add(cycleButton);
		return res;
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		var object = getASWTDSSAsJsonElement(this);
		object.addProperty(getterJsonKey, getter.id);
		return object;
	}
	
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if(element == null) return;
		if (!(element instanceof JsonObject object)) {
			warnFailedLoadingConfig(className, element);
			return;
		}
		setASWTDSSValueFromJsonObject(this, object);
		if(object.get(getterJsonKey) instanceof JsonElement element1){
			if(element1 instanceof JsonPrimitive primitive &&
				StackGetter.fromId(primitive.getAsString()) instanceof StackGetter stackGetter)
				getter = stackGetter;
			else warnFailedLoadingConfig(className + '.' + getterJsonKey, element1);
		}
	}
	
	@Override public @NotNull ScriptNotNullSupplier<ItemStack>
	compileNotNull(CompileEnvironment environment) {
		var compiledEntitySupplier = entity.get().compileCheckedNotNull(environment);
		return map->{
			if(compiledEntitySupplier.scriptApply(map) instanceof LivingEntity livingEntity)
				return getter.getEntityStack(livingEntity);
			else return ItemStack.EMPTY;
		};
	}
}
