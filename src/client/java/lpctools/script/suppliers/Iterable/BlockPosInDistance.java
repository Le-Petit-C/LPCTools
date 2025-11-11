package lpctools.script.suppliers.Iterable;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.exceptions.ScriptRuntimeException;
import lpctools.script.runtimeInterfaces.ScriptNullableFunction;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.script.suppliers.Double.ConstantDouble;
import lpctools.script.suppliers.Vec3d.ConstantVec3d;
import lpctools.util.AlgorithmUtils;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

//从近到远遍历，距离是到方块坐标所表示的方块中心的距离，也就是方块坐标xyz都加了0.5
public class BlockPosInDistance extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IIterableSupplier {
	protected final SupplierStorage<Vec3d> center = ofStorage(Vec3d.class, new ConstantVec3d(this),
		Text.translatable("lpctools.script.suppliers.Iterable.blockPosInDistance.subSuppliers.center.name"), "center");
	protected final SupplierStorage<Double> distance = ofStorage(Double.class, new ConstantDouble(this),
		Text.translatable("lpctools.script.suppliers.Iterable.blockPosInDistance.subSuppliers.distance.name"), "distance");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(center, distance);
	
	public BlockPosInDistance(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNullableFunction<CompileEnvironment.RuntimeVariableMap, ObjectIterable>
	compile(CompileEnvironment variableMap) {
		var compiledCenterSupplier = center.get().compile(variableMap);
		var compiledDistanceSupplier = distance.get().compile(variableMap);
		return map->{
			var center = compiledCenterSupplier.scriptApply(map);
			if(center == null) throw ScriptRuntimeException.nullPointer(this);
			var distance = compiledDistanceSupplier.scriptApply(map);
			if(distance == null) throw ScriptRuntimeException.nullPointer(this);
			return ObjectIterable.of(AlgorithmUtils.iterateFromClosestInDistance(center, distance));
		};
	}
}
