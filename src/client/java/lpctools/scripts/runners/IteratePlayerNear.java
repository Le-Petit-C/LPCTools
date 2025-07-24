package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.DoubleSupplierChooser;
import lpctools.scripts.choosers.RunnerChooser;
import lpctools.scripts.runners.variables.BlockPosVariable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.util.AlgorithmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

public class IteratePlayerNear extends WrappedThirdListConfig implements IScriptRunner {
	public final BlockPosVariable blockPosVariable;
	public final DoubleSupplierChooser distance;
	public final RunnerChooser run;
	public IteratePlayerNear(ILPCConfigReadable parent) {
		super(parent, nameKey, null);
		blockPosVariable = new BlockPosVariable(this){
			@Override public @NotNull String getFullTranslationKey() {return IteratePlayerNear.fullKey + ".variable";}
		};
		distance = new DoubleSupplierChooser(parent, "distance", null);
		run = new RunnerChooser(parent, "run", null);
		addConfig(blockPosVariable);
		addConfig(distance);
		addConfig(run);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> distance.openChoose(), ()->fullKey + ".distance", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> run.openChoose(), ()->fullKey + ".run", buttonGenericAllocator);
	}
	@Override public @NotNull Consumer<CompiledVariableList>
	compile(VariableMap variableMap) throws CompileFailedException {
		ToDoubleFunction<CompiledVariableList> distance;
		Consumer<CompiledVariableList> run;
		Consumer<CompiledVariableList> variable;
		distance = this.distance.get().compileToDouble(variableMap);
		variableMap.push();
		run = this.run.get().compile(variableMap);
		variable = blockPosVariable.compile(variableMap);
		int index = variableMap.get(blockPosVariable.getStringValue(), BlockPosVariable.testPack);
		variableMap.pop();
		return list->{
			double d = distance.applyAsDouble(list);
			Vec3d center;
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player != null) center = player.getEyePos();
			else center = Vec3d.ZERO;
			list.push();
			variable.accept(list);
			BlockPos.Mutable mutable = list.getVariable(index);
			for(BlockPos pos : AlgorithmUtils.iterateFromClosestInDistance(center, d)){
				mutable.set(pos);
				run.accept(list);
			}
			list.pop();
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "iteratePlayerNear";
	public static final String fullKey = fullPrefix + nameKey;
}
