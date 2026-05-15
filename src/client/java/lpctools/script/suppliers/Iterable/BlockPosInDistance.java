package lpctools.script.suppliers.Iterable;

import lpctools.script.CompileEnvironment;
import lpctools.script.IScriptWithSubScript;
import lpctools.script.runtimeInterfaces.ScriptNotNullSupplier;
import lpctools.script.suppliers.AbstractSupplierWithTypeDeterminedSubSuppliers;
import lpctools.util.AlgorithmUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

//从近到远遍历，距离是到方块坐标所表示的方块中心的距离，也就是方块坐标xyz都加了0.5
public class BlockPosInDistance extends AbstractSupplierWithTypeDeterminedSubSuppliers implements IIterableSupplier {
	protected final SupplierStorage<Vec3> center = ofStorage(Vec3.class,
		Component.translatable("lpctools.script.suppliers.iterable.blockPosInDistance.subSuppliers.center.name"), "center");
	protected final SupplierStorage<Double> distance = ofStorage(Double.class,
		Component.translatable("lpctools.script.suppliers.iterable.blockPosInDistance.subSuppliers.distance.name"), "distance");
	protected final SupplierStorage<?>[] subSuppliers = ofStorages(center, distance);
	
	public BlockPosInDistance(IScriptWithSubScript parent) {super(parent);}
	
	@Override protected SupplierStorage<?>[] getSubSuppliers() {return subSuppliers;}
	
	@Override public @NotNull ScriptNotNullSupplier<ObjectIterable>
	compileNotNull(CompileEnvironment environment) {
		var compiledCenterSupplier = center.get().compileCheckedNotNull(environment);
		var compiledDistanceSupplier = distance.get().compileCheckedNotNull(environment);
		return map->ObjectIterable.of(AlgorithmUtils.iterateFromClosestInDistance(compiledCenterSupplier.scriptApply(map), compiledDistanceSupplier.scriptApply(map)));
	}
}
