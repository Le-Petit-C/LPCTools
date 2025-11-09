package lpctools.script.suppliers.Block;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lpctools.lpcfymasaapi.screen.ChooseItemScreen;
import lpctools.lpcfymasaapi.screen.ItemButton;
import lpctools.script.AbstractScript;
import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.editScreen.WidthAutoAdjustButtonGeneric;
import lpctools.script.runtimeInterfaces.ScriptFunction;
import lpctools.util.DataUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static lpctools.lpcfymasaapi.LPCConfigUtils.warnFailedLoadingConfig;

public class ConstantBlock extends AbstractScript implements IBlockSupplier {
	private @NotNull Block block = Blocks.AIR;
	private @Nullable ItemButton itemButton;
	private @Nullable WidthAutoAdjustButtonGeneric selectButton;
	public ConstantBlock(IScriptWithSubScript parent) {super(parent);}
	@Override public @NotNull ScriptFunction<CompileEnvironment.RuntimeVariableMap, Block>
	compile(CompileEnvironment variableMap) {return map->block;}
	
	@Override public @Nullable JsonElement getAsJsonElement() {
		return new JsonPrimitive(DataUtils.getBlockId(block));
	}
	@Override public void setValueFromJsonElement(@Nullable JsonElement element) {
		if (element == null) return;
		if(element instanceof JsonPrimitive primitive &&
			DataUtils.getBlockFromId(primitive.getAsString(), false) instanceof Block b)
			block = b;
		else warnFailedLoadingConfig("ConstantBlock", element);
	}
	@Override public @Nullable Iterable<?> getWidgets() {
		return List.of(getItemButton(), getSelectButton());
	}
	
	public void setBlock(Block block){
		this.block = block;
		getItemButton().setItem(block.asItem());
		getSelectButton().setDisplayString(DataUtils.getBlockId(block));
	}
	
	private @NotNull ItemButton getItemButton(){
		if(itemButton == null) itemButton = new ItemButton(block.asItem(), 0, 0, List.of());
		return itemButton;
	}
	
	private @NotNull WidthAutoAdjustButtonGeneric getSelectButton(){
		if(selectButton == null) {
			selectButton = new WidthAutoAdjustButtonGeneric(
				getDisplayWidget(), 0, 0, 20, DataUtils.getBlockId(block), null);
			selectButton.setActionListener(
				(button, mouseButton)->{
					ChooseItemScreen screen = ChooseItemScreen.ofAllBlocks(9, 6, this::setBlock);
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
