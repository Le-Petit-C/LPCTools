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
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockConfig extends LPCUniqueConfigBase {
	protected Block block;
	public final @NotNull Block defaultValue;
	public BlockConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull Block defaultValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		this.block = this.defaultValue = defaultValue;
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(-1, (IButtonActionListener) null, null, (x, y, w, h, key, listener, consumer, reset)->
			consumer.addButton(new ItemButton(block.asItem(), x + w / 2, y + h / 2, List.of(block.getName().getString(), DataUtils.getBlockId(block))), null));
		res.add(1, (b, m)->chooseBlock(), block.getName()::getString, buttonGenericAllocator);
	}
	public Block getBlock(){return block;}
	private void chooseBlock(){
		ChooseItemScreen screen = ChooseItemScreen.ofAllBlocks(9, 6, block->this.block = block);
		MinecraftClient.getInstance().setScreen(screen);
	}
	@Override public @Nullable JsonElement getAsJsonElement() {
		return new JsonPrimitive(DataUtils.getBlockId(block));
	}
	
	@Override
	public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		if(element instanceof JsonPrimitive primitive){
			var block = DataUtils.getBlockFromId(primitive.getAsString(), false);
			if(block != null){
				var res = new UpdateTodo().valueChanged(this.block != block);
				this.block = block;
				return res;
			}
		}
		return setValueFailed(element);
	}
}
