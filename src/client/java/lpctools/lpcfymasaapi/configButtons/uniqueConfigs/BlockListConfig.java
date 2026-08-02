package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.CachedSupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockListConfig extends ConfigListConfig<BlockConfig> {
	private final CachedSupplier<ImmutableSet<Block>> blocks = new CachedSupplier<>(this::buildBlocks);
	private boolean suppressValueChanged = false;
	
	public BlockListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends Block> defaultValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, config->new BlockConfig(config, "block", Blocks.AIR, config::onValueChanged), callback);
		if (defaultValue != null) {
			setBlocks(defaultValue);
			setCurrentAsDefault(false);
		}
	}
	
	public BlockListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends Block> defaultValue) {
		this(parent, nameKey, defaultValue, null);
	}
	
	public boolean contains(Block block) { return blocks.get().contains(block); }
	public ImmutableSet<Block> getBlocks() { return blocks.get(); }
	
	public void setBlocks(Iterable<? extends Block> blocks) {
		suppressValueChanged = true;
		subConfigs.clear();
		for(var block : blocks) allocateAndAddConfig().setBlock(block);
		suppressValueChanged = false;
		onValueChanged();
	}
	
	@Override public void onValueChanged() {
		if(suppressValueChanged) return;
		blocks.invalidate();
		super.onValueChanged();
	}

	private ImmutableSet<Block> buildBlocks() {
		ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
		for(var config : iterateConfigs()) builder.add(config.getBlock());
		return builder.build();
	}
}
