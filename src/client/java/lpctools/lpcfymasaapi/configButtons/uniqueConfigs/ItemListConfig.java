package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.common.collect.ImmutableSet;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemListConfig extends ConfigListConfig<ItemConfig> {
	private ImmutableSet<Item> items = ImmutableSet.of();
	private boolean suppressValueChanged = false;
	
	public ItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends Item> defaultValue, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, config->new ItemConfig(config, "item", Items.AIR, config::onValueChanged), callback);
		if (defaultValue != null) {
			setItems(defaultValue);
			setCurrentAsDefault(false);
		}
	}
	
	public ItemListConfig(@NotNull ILPCConfigReadable parent, String nameKey, @Nullable Iterable<? extends Item> defaultValue) {
		this(parent, nameKey, defaultValue, null);
	}
	
	public boolean contains(Item item){
		return items.contains(item);
	}
	
	@SuppressWarnings("unused")
	public ImmutableSet<Item> getItems(){
		return items;
	}
	
	public void setItems(Iterable<? extends Item> blocks) {
		suppressValueChanged = true;
		subConfigs.clear();
		for(var block : blocks) allocateAndAddConfig().setBlock(block);
		suppressValueChanged = false;
		onValueChanged();
	}
	
	@Override public void onValueChanged() {
		if(suppressValueChanged) return;
		ImmutableSet.Builder<Item> builder = ImmutableSet.builder();
		for(var config : iterateConfigs()) builder.add(config.getItem());
		items = builder.build();
		super.onValueChanged();
	}
}
