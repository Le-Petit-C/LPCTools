package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockItemListConfig extends ConfigListConfig<BlockItemConfig> {
	private ImmutableSet<BlockItem> blockItems = ImmutableSet.of();
	private ImmutableSet<Block> blocks = ImmutableSet.of();
	private boolean needUpdate = true;
	
	public BlockItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends BlockItem> defaultValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, config->new BlockItemConfig(config, "blockItem", (BlockItem) Items.STONE, config::onValueChanged), callback);
		if (defaultValue != null) {
			setBlockItems(defaultValue);
			setCurrentAsDefault(false);
		}
	}
	
	public BlockItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends BlockItem> defaultValue) {
		this(parent, nameKey, defaultValue, null);
	}
	
	public boolean contains(BlockItem blockItem){
		tryUpdate();
		return blockItems.contains(blockItem);
	}
	
	public boolean contains(Block block){
		tryUpdate();
		return blocks.contains(block);
	}
	
	public ImmutableSet<BlockItem> getBlockItems(){
		tryUpdate();
		return blockItems;
	}
	
	public ImmutableSet<Block> getBlocks(){
		tryUpdate();
		return blocks;
	}
	
	public void setBlockItems(Iterable<? extends BlockItem> blockItems) {
		subConfigs.clear();
		for(var blockItem : blockItems) allocateAndAddConfig().setBlockItem(blockItem);
		onValueChanged();
	}
	
	public void tryUpdate(){
		if(!needUpdate) return;
		needUpdate = false;
		ImmutableSet.Builder<BlockItem> builder = ImmutableSet.builder();
		ImmutableSet.Builder<Block> blockSetBuilder = ImmutableSet.builder();
		for(var config : iterateConfigs()) {
			var blockItem = config.getBlockItem();
			builder.add(blockItem);
			blockSetBuilder.add(blockItem.getBlock());
		}
		blockItems = builder.build();
		blocks = blockSetBuilder.build();
	}
	
	@Override public void onValueChanged() {
		needUpdate = true;
		super.onValueChanged();
	}
}
