package lpctools.scripts.runners;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.WrappedThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.choosers.DoubleSupplierChooser;
import lpctools.scripts.choosers.RunnerChooser;
import lpctools.scripts.choosers.Vector3dSupplierChooser;
import lpctools.scripts.runners.variables.BlockPosVariable;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.util.AlgorithmUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

public class IterateFromClosestInDistance extends WrappedThirdListConfig implements IScriptRunner {
	public final BlockPosVariable blockPosVariable = addConfig(new BlockPosVariable(this){
		@Override public @NotNull String getFullTranslationKey() {return IterateFromClosestInDistance.fullKey + ".variable";}
	});
	public final Vector3dSupplierChooser center = addConfig(new Vector3dSupplierChooser(parent, "center", null));
	public final DoubleSupplierChooser distance = addConfig(new DoubleSupplierChooser(parent, "distance", null));
	public final RunnerChooser run = addConfig(new RunnerChooser(parent, "run", null));
	public IterateFromClosestInDistance(ILPCConfigReadable parent) {super(parent, nameKey, null);}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(1, (button, mouseButton) -> distance.openChoose(), ()->fullKey + ".distance", buttonGenericAllocator);
		res.add(1, (button, mouseButton) -> run.openChoose(), ()->fullKey + ".run", buttonGenericAllocator);
	}
	@Override public @NotNull Consumer<CompiledVariableList>
	compile(VariableMap variableMap) throws CompileFailedException {
		BiConsumer<CompiledVariableList, Vector3d> center = this.center.get().compileToVector3d(variableMap);
		ToDoubleFunction<CompiledVariableList> distance = this.distance.get().compileToDouble(variableMap);
		variableMap.push();
		Consumer<CompiledVariableList> variable = blockPosVariable.compile(variableMap);
		Consumer<CompiledVariableList> run = this.run.get().compile(variableMap);
		int index = variableMap.get(blockPosVariable.getStringValue(), BlockPosVariable.testPack);
		variableMap.pop();
		Vector3d bufCenter = new Vector3d();
		return list->{
			center.accept(list, bufCenter);
			double d = distance.applyAsDouble(list);
			list.push();
			variable.accept(list);
			BlockPos.Mutable mutable = list.getVariable(index);
			for(BlockPos pos : AlgorithmUtils.iterateFromClosestInDistance(new Vec3d(bufCenter.x, bufCenter.y, bufCenter.z), d)){
				mutable.set(pos);
				run.accept(list);
			}
			list.pop();
		};
	}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "iterateFromClosestInDistance";
	public static final String fullKey = fullPrefix + nameKey;
}
