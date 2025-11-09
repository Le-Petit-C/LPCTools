package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.lpcfymasaapi.screen.ChooseItemScreen;
import lpctools.lpcfymasaapi.screen.ItemButton;
import lpctools.util.DataUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemConfig extends LPCUniqueConfigBase {
	protected Item item;
	public final @NotNull Item defaultValue;
	public ItemConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull Item defaultValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		this.item = this.defaultValue = defaultValue;
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(-1, (IButtonActionListener) null, null, (x, y, w, h, key, listener, consumer, reset)->
			consumer.addButton(new ItemButton(item, x + w / 2, y + h / 2, List.of(item.getName().getString(), DataUtils.getItemId(item))), null));
		res.add(1, (b, m)->chooseBlock(), item.getName()::getString, buttonGenericAllocator);
	}
	public Item getItem(){return item;}
	public void setBlock(Item item){
		this.item = item;
		onValueChanged();
	}
	
	private void chooseBlock(){
		ChooseItemScreen screen = ChooseItemScreen.ofAllItems(9, 6, item->{
			this.item = item;
			onValueChanged();
		});
		MinecraftClient client = MinecraftClient.getInstance();
		client.currentScreen = null;
		MinecraftClient.getInstance().setScreen(screen);
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {return new JsonPrimitive(DataUtils.getItemId(item));}
	
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		if(element instanceof JsonPrimitive primitive){
			var item = DataUtils.getItemFromId(primitive.getAsString(), false);
			if(item != null){
				var res = new UpdateTodo().valueChanged(this.item != item);
				this.item = item;
				return res;
			}
		}
		return setValueFailed(element);
	}
}
