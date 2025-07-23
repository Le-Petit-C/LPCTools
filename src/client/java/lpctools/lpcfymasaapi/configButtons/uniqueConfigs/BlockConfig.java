package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockConfig extends LPCUniqueConfigBase {
	protected Block block;
	public final @NotNull Block defaultValue;
	public BlockConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @NotNull Block defaultValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		this.block = this.defaultValue = defaultValue;
	}
	
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(1, (b, m)->chooseBlock(), block.getName()::getString, buttonGenericAllocator);
	}
	
	private void chooseBlock(){
	}
	
	@Override
	public @Nullable JsonElement getAsJsonElement() {
		return null;
	}
	
	@Override
	public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		return null;
	}
}
