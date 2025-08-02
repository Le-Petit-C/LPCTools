package lpctools.scripts.suppliers._int;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.utils.choosers.BlockPosSupplierChooser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.ToIntFunction;

public class SkyLightLevel extends WrappedThirdListConfig implements IScriptIntSupplier {
	private final BlockPosSupplierChooser pos = addConfig(new BlockPosSupplierChooser(parent, "pos", this::onValueChanged));
	public SkyLightLevel(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> pos.openChoose(), ()->fullKey + ".pos", buttonGenericAllocator);
	}
	@Override public @NotNull ToIntFunction<CompiledVariableList>
	compileToInt(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, BlockPos.Mutable> state = this.pos.get().compileToBlockPos(variableMap);
		BlockPos.Mutable buf = new BlockPos.Mutable();
		return list->{
			ClientWorld world = MinecraftClient.getInstance().world;
			if(world != null){
				state.accept(list, buf);
				return world.getLightingProvider().get(LightType.SKY).getLightLevel(buf);
			}
			else return 0;
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "skyLightLevel";
	public static final String fullKey = fullPrefix + nameKey;
}
