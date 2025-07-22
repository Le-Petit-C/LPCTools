package lpctools.scripts.suppliers.blockPos;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class BlockPosAdd extends ThirdListConfig implements IScriptBlockPosSupplier {
	private final BlockPosSupplierChooser pos1, pos2;
	public BlockPosAdd(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		pos1 = new BlockPosSupplierChooser(parent, "pos1", this::onValueChanged);
		pos2 = new BlockPosSupplierChooser(parent, "pos2", this::onValueChanged);
		refreshConfigList();
	}
	private void refreshConfigList(){
		getConfigs().clear();
		addConfig(pos1.get());
		addConfig(pos2.get());
	}
	private ThirdListConfig prepareJson(){
		ThirdListConfig list = new ThirdListConfig(getParent(), nameKey, null);
		list.setExpanded(isExpanded());
		list.addConfig(pos1);
		list.addConfig(pos2);
		return list;
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos1.openChoose(), ()->fullKey + ".pos1", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> pos2.openChoose(), ()->fullKey + ".pos2", buttonGenericAllocator);
	}
	@Override public @NotNull Function<CompiledVariableList, BlockPos> compile(VariableMap variableMap) throws CompileFailedException {
		Function<CompiledVariableList, BlockPos> pos1, pos2;
		pos1 = this.pos1.get().compile(variableMap);
		pos2 = this.pos2.get().compile(variableMap);
		return list->pos1.apply(list).add(pos2.apply(list));
	}
	@Override public @NotNull JsonObject getAsJsonElement() {
		return prepareJson().getAsJsonElement();
	}
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		ThirdListConfig config = prepareJson();
		UpdateTodo todo = config.setValueFromJsonElementEx(element);
		setExpanded(config.isExpanded());
		return todo;
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	@Override public void onValueChanged() {
		refreshConfigList();
		notifyScriptChanged();
		super.onValueChanged();
	}
	public static final String nameKey = "blockPosAdd";
	public static final String fullKey = fullPrefix + nameKey;
}
