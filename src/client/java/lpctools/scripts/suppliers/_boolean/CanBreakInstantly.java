package lpctools.scripts.suppliers._boolean;

import fi.dy.masa.litematica.util.ToBooleanFunction;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.util.BlockUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class CanBreakInstantly extends WrappedThirdListConfig implements IScriptBooleanSupplier {
	private final BlockPosSupplierChooser pos = new BlockPosSupplierChooser(parent, "pos", this::onValueChanged);
	public CanBreakInstantly(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		addConfig(pos);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
	}
	@Override public @NotNull ToBooleanFunction<CompiledVariableList>
	compileToBoolean(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> pos = this.pos.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf = new BlockPos.Mutable();
		return list->{
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player != null) {
				pos.accept(list, buf);
				return BlockUtils.canBreakInstantly(player, buf);
			}
			else return false;
		};
	}
	@Override public void onValueChanged() {
		getPage().markNeedUpdate();
		super.onValueChanged();
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "canBreakInstantly";
	public static final String fullKey = fullPrefix + nameKey;
	
}
