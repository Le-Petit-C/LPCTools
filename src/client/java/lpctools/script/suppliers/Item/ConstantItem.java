package lpctools.script.suppliers.Item;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.screen.ChooseItemScreen;
import lpctools.lpcfymasaapi.screen.ItemButton;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.util.DataUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantItem extends AbstractScript implements IItemSupplier {
	private @NotNull Item item = Items.AIR;
	private @Nullable ItemButton itemButton;
	private @Nullable WidthAutoAdjustButtonGeneric selectButton;
	public ConstantItem(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, Item>
	compile(CompileEnvironment variableMap) {return map->item;}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(DataUtils.getItemId(item));}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if (element == null) return;
		if(element instanceof JsonPrimitive primitive &&
			DataUtils.getItemFromId(primitive.getAsString(), false) instanceof Item i)
			item = i;
		else warnFailedLoadingConfig("ConstantItem", element);
	}
	@Override public @Nullable Iterable<?> getWidgets() {return List.of(getItemButton(), getSelectButton());}
	
	public void setItem(Item item){
		this.item = item;
		getItemButton().setItem(item);
		getSelectButton().setDisplayString(DataUtils.getItemId(item));
	}
	
	private @NotNull ItemButton getItemButton(){
		if(itemButton == null) itemButton = new ItemButton(item.asItem(), 0, 0, List.of());
		return itemButton;
	}
	
	private @NotNull WidthAutoAdjustButtonGeneric getSelectButton(){
		if(selectButton == null) {
			selectButton = new WidthAutoAdjustButtonGeneric(
				getDisplayWidget(), 0, 0, 20, DataUtils.getItemId(item), null);
			selectButton.setActionListener(
				(button, mouseButton)->{
					ChooseItemScreen screen = ChooseItemScreen.ofAllItems(9, 6, this::setItem);
					var client = MinecraftClient.getInstance();
					screen.setParent(client.currentScreen);
					client.currentScreen = null;
					MinecraftClient.getInstance().setScreen(screen);
				}
			);
		}
		return selectButton;
	}
}
