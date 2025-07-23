package lpctools.scripts.suppliers._boolean;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class BlockPosEquals extends ThirdListConfig implements IScriptBooleanSupplier {
	private final BlockPosSupplierChooser pos1, pos2;
	public BlockPosEquals(ILPCConfigReadable parent) {
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
	@Override
	public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos1, pos2;
		pos1 = this.pos1.get().compileToBlockPos(variableMap);
		pos2 = this.pos2.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf1 = new BlockPos.Mutable();
		BlockPos.Mutable buf2 = new BlockPos.Mutable();
		return list->{
			pos1.accept(list, buf1);
			pos2.accept(list, buf2);
			return buf1.equals(buf2);
		};
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
	@Override public void onValueChanged() {
		refreshConfigList();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "blockPosEquals";
	public static final String fullKey = fullPrefix + nameKey;
	
}
