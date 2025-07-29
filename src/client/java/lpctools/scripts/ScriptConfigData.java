package lpctools.scripts;

import com.google.common.collect.ImmutableMap;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.runners.*;
import lpctools.scripts.runners.setVariable.*;
import lpctools.scripts.runners.variables.*;
import lpctools.scripts.suppliers._boolean.*;
import lpctools.scripts.suppliers._double.*;
import lpctools.scripts.suppliers._int.*;
import lpctools.scripts.suppliers.axis.*;
import lpctools.scripts.suppliers.block.*;
import lpctools.scripts.suppliers.blockPos.*;
import lpctools.scripts.suppliers.direction.*;
import lpctools.scripts.suppliers.vector3d.*;
import lpctools.scripts.trigger.*;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public class ScriptConfigData {
	public static final ImmutableMap<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>> triggerConfigs =
		ImmutableMap.<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>>builder()
			.put(TriggerHotkey.nameKey, (p, k, r)->new TriggerHotkey(p, r))
			.put(TriggerClientTickStart.nameKey, (p, k, r)->new TriggerClientTickStart(p, r))
			.put(TriggerClientTickEnd.nameKey, (p, k, r)->new TriggerClientTickEnd(p, r))
			.build();
	public static final ImmutableMap<String, Object> triggerConfigsTree = treeBuilder()
		.put(TriggerHotkey.fullKey)
		.put(TriggerClientTickStart.fullKey)
		.put(TriggerClientTickEnd.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptRunner>> runnerConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptRunner>>builder()
			.put(DoNothing.nameKey, (p, k)->new DoNothing(p))
			.put(BooleanVariable.nameKey, (p, k)->new BooleanVariable(p))
			.put(IntVariable.nameKey, (p, k)->new IntVariable(p))
			.put(DoubleVariable.nameKey, (p, k)->new DoubleVariable(p))
			.put(BlockPosVariable.nameKey, (p, k)->new BlockPosVariable(p))
			.put(Vector3dVariable.nameKey, (p, k)->new Vector3dVariable(p))
			.put(BlockVariable.nameKey, (p, k)->new BlockVariable(p))
			.put(DirectionVariable.nameKey, (p, k)->new DirectionVariable(p))
			.put(AxisVariable.nameKey, (p, k)->new AxisVariable(p))
			.put(SetBooleanVariable.nameKey, (p, k)->new SetBooleanVariable(p))
			.put(SetIntVariable.nameKey, (p, k)->new SetIntVariable(p))
			.put(SetDoubleVariable.nameKey, (p, k)->new SetDoubleVariable(p))
			.put(SetBlockPosVariable.nameKey, (p, k)->new SetBlockPosVariable(p))
			.put(SetVector3dVariable.nameKey, (p, k)->new SetVector3dVariable(p))
			.put(SetBlockVariable.nameKey, (p, k)->new SetBlockVariable(p))
			.put(SetDirectionVariable.nameKey, (p, k)->new SetDirectionVariable(p))
			.put(SetAxisVariable.nameKey, (p, k)->new SetAxisVariable(p))
			.put(SubRunners.nameKey, (p, k)->new SubRunners(p))
			.put(RunIfElse.nameKey, (p, k)->new RunIfElse(p))
			.put(WhileLoop.nameKey, (p, k)->new WhileLoop(p))
			.put(ForLoop.nameKey, (p, k)->new ForLoop(p))
			.put(InteractBlock.nameKey, (p, k)->new InteractBlock(p))
			.put(AttackBlock.nameKey, (p, k)->new AttackBlock(p))
			.put(RunnerMessage.nameKey, (p, k)->new RunnerMessage(p))
			.put(IterateFromClosestInDistance.nameKey, (p, k)->new IterateFromClosestInDistance(p))
			.build();
	public static final ImmutableMap<String, Object> runnerConfigsTree = treeBuilder()
		.put(DoNothing.fullKey)
		.put(Variable.fullKey, treeBuilder()
			.put(BooleanVariable.fullKey)
			.put(IntVariable.fullKey)
			.put(DoubleVariable.fullKey)
			.put(BlockPosVariable.fullKey)
			.put(Vector3dVariable.fullKey)
			.put(BlockVariable.fullKey)
			.put(DirectionVariable.fullKey)
			.put(AxisVariable.fullKey)
			.build())
		.put(SetVariable.fullKey, treeBuilder()
			.put(SetBooleanVariable.fullKey)
			.put(SetIntVariable.fullKey)
			.put(SetDoubleVariable.fullKey)
			.put(SetBlockPosVariable.fullKey)
			.put(SetVector3dVariable.fullKey)
			.put(SetBlockVariable.fullKey)
			.put(SetDirectionVariable.fullKey)
			.put(SetAxisVariable.fullKey)
			.build())
		.put(RunIfElse.fullKey)
		.put(SubRunners.fullKey)
		.put(WhileLoop.fullKey)
		.put(ForLoop.fullKey)
		.put(InteractBlock.fullKey)
		.put(AttackBlock.fullKey)
		.put(RunnerMessage.fullKey)
		.put(IterateFromClosestInDistance.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptBooleanSupplier>> booleanSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptBooleanSupplier>>builder()
			.put(StaticBoolean.nameKey, (p, k) -> new StaticBoolean(p))
			.put(FromBooleanVariable.nameKey, (p, k)->new FromBooleanVariable(p))
			.put(Not.nameKey, (p, k)->new Not(p))
			.put(AnyTrue.nameKey, (p, k)->new AnyTrue(p))
			.put(EveryTrue.nameKey, (p, k)->new EveryTrue(p))
			.put(AnyFalse.nameKey, (p, k)->new AnyFalse(p))
			.put(EveryFalse.nameKey, (p, k)->new EveryFalse(p))
			.put(IntCompare.nameKey, (p, k)->new IntCompare(p))
			.put(DoubleCompare.nameKey, (p, k)->new DoubleCompare(p))
			.put(BooleanCompare.nameKey, (p, k)->new BooleanCompare(p))
			.put(BlockPosCompare.nameKey, (p, k)->new BlockPosCompare(p))
			.put(BlockCompare.nameKey, (p, k)->new BlockCompare(p))
			.put(CanBreakInstantly.nameKey, (p, k)->new CanBreakInstantly(p))
			.build();
	public static final ImmutableMap<String, Object> booleanSupplierConfigsTree = treeBuilder()
		.put(StaticBoolean.fullKey)
		.put(FromBooleanVariable.fullKey)
		.put(Not.fullKey)
		.put(AnyTrue.fullKey)
		.put(EveryTrue.fullKey)
		.put(AnyFalse.fullKey)
		.put(EveryFalse.fullKey)
		.put(IntCompare.fullKey)
		.put(DoubleCompare.fullKey)
		.put(BooleanCompare.fullKey)
		.put(BlockPosCompare.fullKey)
		.put(BlockCompare.fullKey)
		.put(CanBreakInstantly.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptIntSupplier>> intSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptIntSupplier>>builder()
			.put(StaticInt.nameKey, (p, k) -> new StaticInt(p))
			.put(FromIntVariable.nameKey, (p, k)->new FromIntVariable(p))
			.put(IntCalculate.nameKey, (p, k)->new IntCalculate(p))
			.put(IntOpposite.nameKey, (p, k)->new IntOpposite(p))
			.put(RoundingDouble.nameKey, (p, k)->new RoundingDouble(p))
			.put(IntFromBlockPos.nameKey, (p, k)->new IntFromBlockPos(p))
			.put(BlockPosDotProduct.nameKey, (p, k)->new BlockPosDotProduct(p))
			.build();
	public static final ImmutableMap<String, Object> intSupplierConfigsTree = treeBuilder()
		.put(StaticInt.fullKey)
		.put(FromIntVariable.fullKey)
		.put(IntCalculate.fullKey)
		.put(IntOpposite.fullKey)
		.put(RoundingDouble.fullKey)
		.put(IntFromBlockPos.fullKey)
		.put(BlockPosDotProduct.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptDoubleSupplier>> doubleSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptDoubleSupplier>>builder()
			.put(StaticDouble.nameKey, (p, k)->new StaticDouble(p))
			.put(FromDoubleVariable.nameKey, (p, k)->new FromDoubleVariable(p))
			.put(DoubleCalculate.nameKey, (p, k)->new DoubleCalculate(p))
			.put(DoubleOpposite.nameKey, (p, k)->new DoubleOpposite(p))
			.put(FromInt.nameKey, (p, k)->new FromInt(p))
			.put(DoubleFromVector3d.nameKey, (p, k)->new DoubleFromVector3d(p))
			.put(Vector3dDotProduct.nameKey, (p, k)->new Vector3dDotProduct(p))
			.put(PlayerInteractionRange.nameKey, (p, k)->new PlayerInteractionRange(p))
			.build();
	public static final ImmutableMap<String, Object> doubleSupplierConfigsTree = treeBuilder()
		.put(StaticDouble.fullKey)
		.put(FromDoubleVariable.fullKey)
		.put(DoubleCalculate.fullKey)
		.put(DoubleOpposite.fullKey)
		.put(FromInt.fullKey)
		.put(DoubleFromVector3d.fullKey)
		.put(Vector3dDotProduct.fullKey)
		.put(PlayerInteractionRange.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptBlockPosSupplier>> blockPosSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptBlockPosSupplier>>builder()
			.put(StaticBlockPos.nameKey, (p, k) -> new StaticBlockPos(p))
			.put(FromBlockPosVariable.nameKey, (p, k) -> new FromBlockPosVariable(p))
			.put(BlockPosFromInts.nameKey, (p, k) -> new BlockPosFromInts(p))
			.put(BlockPosCalculate.nameKey, (p, k) -> new BlockPosCalculate(p))
			.put(BlockPosCrossProduct.nameKey, (p, k) -> new BlockPosCrossProduct(p))
			.put(BlockPosScalarMul.nameKey, (p, k) -> new BlockPosScalarMul(p))
			.put(RoundingVector3d.nameKey, (p, k) -> new RoundingVector3d(p))
			.put(BlockPosFromDirection.nameKey, (p, k) -> new BlockPosFromDirection(p))
			.put(PlayerBlockPos.nameKey, (p, k) -> new PlayerBlockPos(p))
			.build();
	public static final ImmutableMap<String, Object> blockPosSupplierConfigsTree = treeBuilder()
		.put(StaticBlockPos.fullKey)
		.put(FromBlockPosVariable.fullKey)
		.put(BlockPosFromInts.fullKey)
		.put(BlockPosCalculate.fullKey)
		.put(BlockPosCrossProduct.fullKey)
		.put(BlockPosScalarMul.fullKey)
		.put(RoundingVector3d.fullKey)
		.put(BlockPosFromDirection.fullKey)
		.put(PlayerBlockPos.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptVector3dSupplier>> vector3dSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptVector3dSupplier>>builder()
			.put(StaticVector3d.nameKey, (p, k) -> new StaticVector3d(p))
			.put(FromVector3dVariable.nameKey, (p, k) -> new FromVector3dVariable(p))
			.put(Vector3dFromDoubles.nameKey, (p, k) -> new Vector3dFromDoubles(p))
			.put(Vector3dCalculate.nameKey, (p, k) -> new Vector3dCalculate(p))
			.put(Vector3dCrossProduct.nameKey, (p, k) -> new Vector3dCrossProduct(p))
			.put(Vector3dScalarMul.nameKey, (p, k) -> new Vector3dScalarMul(p))
			.put(Vector3dFromBlockPos.nameKey, (p, k) -> new Vector3dFromBlockPos(p))
			.put(Vector3dFromDirection.nameKey, (p, k) -> new Vector3dFromDirection(p))
			.put(BlockCenterPos.nameKey, (p, k) -> new BlockCenterPos(p))
			.put(PlayerPos.nameKey, (p, k) -> new PlayerPos(p))
			.put(PlayerEyePos.nameKey, (p, k) -> new PlayerEyePos(p))
			.put(PlayerRotationVector.nameKey, (p, k) -> new PlayerRotationVector(p))
			.build();
	public static final ImmutableMap<String, Object> vector3dSupplierConfigsTree = treeBuilder()
		.put(StaticVector3d.fullKey)
		.put(FromVector3dVariable.fullKey)
		.put(Vector3dFromDoubles.fullKey)
		.put(Vector3dCalculate.fullKey)
		.put(Vector3dCrossProduct.fullKey)
		.put(Vector3dScalarMul.fullKey)
		.put(Vector3dFromBlockPos.fullKey)
		.put(Vector3dFromDirection.fullKey)
		.put(BlockCenterPos.fullKey)
		.put(PlayerPos.fullKey)
		.put(PlayerEyePos.fullKey)
		.put(PlayerRotationVector.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptDirectionSupplier>> directionSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptDirectionSupplier>>builder()
			.put(StaticDirection.nameKey, (p, k) -> new StaticDirection(p))
			.put(FromDirectionVariable.nameKey, (p, k) -> new FromDirectionVariable(p))
			.put(DirectionOpposite.nameKey, (p, k) -> new DirectionOpposite(p))
			.put(DirectionFromVector3d.nameKey, (p, k) -> new DirectionFromVector3d(p))
			.put(DirectionFromAxis.nameKey, (p, k) -> new DirectionFromAxis(p))
			.build();
	public static final ImmutableMap<String, Object> directionSupplierConfigsTree = treeBuilder()
		.put(StaticDirection.fullKey)
		.put(FromDirectionVariable.fullKey)
		.put(DirectionOpposite.fullKey)
		.put(DirectionFromVector3d.fullKey)
		.put(DirectionFromAxis.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptAxisSupplier>> axisSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptAxisSupplier>>builder()
			.put(StaticAxis.nameKey, (p, k) -> new StaticAxis(p))
			.put(FromAxisVariable.nameKey, (p, k) -> new FromAxisVariable(p))
			.put(DirectionAxis.nameKey, (p, k) -> new DirectionAxis(p))
			.build();
	public static final ImmutableMap<String, Object> axisSupplierConfigsTree = treeBuilder()
		.put(StaticAxis.fullKey)
		.put(FromAxisVariable.fullKey)
		.put(DirectionAxis.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptBlockSupplier>> blockSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptBlockSupplier>>builder()
			.put(StaticBlock.nameKey, (p, k) -> new StaticBlock(p))
			.put(FromBlockVariable.nameKey, (p, k) -> new FromBlockVariable(p))
			.put(FromWorld.nameKey, (p, k) -> new FromWorld(p))
			.build();
	public static final ImmutableMap<String, Object> blockSupplierConfigsTree = treeBuilder()
		.put(StaticBlock.fullKey)
		.put(FromBlockVariable.fullKey)
		.put(FromWorld.fullKey)
		.build();
	private static class TreeBuilder extends ImmutableMap.Builder<String, Object>{
		public TreeBuilder put(String k, ImmutableMap<String, Object> subTree){
			super.put(k, subTree);
			return this;
		}
		public TreeBuilder put(String k){
			int i = k.lastIndexOf('.');
			String v = k.substring(i + 1);
			super.put(k, v);
			return this;
		}
	}
	private static TreeBuilder treeBuilder(){return new TreeBuilder();}
}
