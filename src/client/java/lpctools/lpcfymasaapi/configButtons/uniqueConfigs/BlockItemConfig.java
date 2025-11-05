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
import net.minecraft.item.BlockItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockItemConfig extends LPCUniqueConfigBase {
	protected BlockItem blockItem;
	public final @NotNull BlockItem defaultValue;
	public BlockItemConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull BlockItem defaultValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		this.blockItem = this.defaultValue = defaultValue;
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(-1, (IButtonActionListener) null, null, (x, y, w, h, key, listener, consumer, reset)->
			consumer.addButton(new ItemButton(blockItem, x + w / 2, y + h / 2, List.of(blockItem.getName().getString(), DataUtils.getItemId(blockItem))), null));
		res.add(1, (b, m)->chooseBlock(), blockItem.getName()::getString, buttonGenericAllocator);
	}
	public BlockItem getBlockItem(){return blockItem;}
	
	public void setBlockItem(BlockItem blockItem){
		this.blockItem = blockItem;
		onValueChanged();
	}
	
	private void chooseBlock(){
		ChooseItemScreen screen = ChooseItemScreen.ofAllBlockItems(9, 6, blockItem->{
			this.blockItem = blockItem;
			onValueChanged();
		});
		MinecraftClient.getInstance().setScreen(screen);
	}
	
	public static JsonPrimitive getBlockConfigAsJsonElement(BlockItemConfig blockConfig){
		return new JsonPrimitive(DataUtils.getItemId(blockConfig.blockItem));
	}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return getBlockConfigAsJsonElement(this);
	}
	
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		if(element instanceof JsonPrimitive primitive){
			var blockItem = DataUtils.getBlockItemFromId(primitive.getAsString(), false);
			if(blockItem != null){
				var res = new UpdateTodo().valueChanged(this.blockItem != blockItem);
				this.blockItem = blockItem;
				return res;
			}
		}
		return setValueFailed(element);
	}
}
