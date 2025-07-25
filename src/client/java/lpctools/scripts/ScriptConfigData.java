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
import lpctools.scripts.suppliers.block.*;
import lpctools.scripts.suppliers.blockPos.*;
import lpctools.scripts.suppliers.direction6.IScriptDirection6Supplier;
import lpctools.scripts.suppliers.direction6.StaticDirection6;
import lpctools.scripts.suppliers.vector3d.*;
import lpctools.scripts.trigger.*;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public class ScriptConfigData {
	public static final ImmutableMap<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>> triggerConfigs =
		ImmutableMap.<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>>builder()
			.put(TriggerHotkey.nameKey, (p, k, r)->new TriggerHotkey(p, r))
			.build();
	public static final ImmutableMap<String, Object> triggerConfigsTree = treeBuilder()
		.put(TriggerHotkey.fullKey)
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
			.put(SetBooleanVariable.nameKey, (p, k)->new SetBooleanVariable(p))
			.put(SetIntVariable.nameKey, (p, k)->new SetIntVariable(p))
			.put(SetDoubleVariable.nameKey, (p, k)->new DoubleVariable(p))
			.put(SetBlockPosVariable.nameKey, (p, k)->new SetBlockPosVariable(p))
			.put(SetVector3dVariable.nameKey, (p, k)->new SetVector3dVariable(p))
			.put(SetBlockVariable.nameKey, (p, k)->new SetBlockVariable(p))
			.put(SubRunners.nameKey, (p, k)->new SubRunners(p))
			.put(RunIfElse.nameKey, (p, k)->new RunIfElse(p))
			.put(WhileLoop.nameKey, (p, k)->new WhileLoop(p))
			.put(ForLoop.nameKey, (p, k)->new ForLoop(p))
			.put(InteractBlock.nameKey, (p, k)->new InteractBlock(p))
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
			.build())
		.put(SetVariable.fullKey, treeBuilder()
			.put(SetBooleanVariable.fullKey)
			.put(SetIntVariable.fullKey)
			.put(SetDoubleVariable.fullKey)
			.put(SetBlockPosVariable.fullKey)
			.put(SetVector3dVariable.fullKey)
			.put(SetBlockVariable.fullKey)
			.build())
		.put(RunIfElse.fullKey)
		.put(SubRunners.fullKey)
		.put(WhileLoop.fullKey)
		.put(ForLoop.fullKey)
		.put(InteractBlock.fullKey)
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
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptIntSupplier>> intSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptIntSupplier>>builder()
			.put(StaticInt.nameKey, (p, k) -> new StaticInt(p))
			.put(FromIntVariable.nameKey, (p, k)->new FromIntVariable(p))
			.build();
	public static final ImmutableMap<String, Object> intSupplierConfigsTree = treeBuilder()
		.put(StaticInt.fullKey)
		.put(FromIntVariable.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptDoubleSupplier>> doubleSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptDoubleSupplier>>builder()
			.put(StaticDouble.nameKey, (p, k)->new StaticDouble(p))
			.put(FromDoubleVariable.nameKey, (p, k)->new FromDoubleVariable(p))
			.put(PlayerInteractionRange.nameKey, (p, k)->new PlayerInteractionRange(p))
			.build();
	public static final ImmutableMap<String, Object> doubleSupplierConfigsTree = treeBuilder()
		.put(StaticDouble.fullKey)
		.put(FromDoubleVariable.fullKey)
		.put(PlayerInteractionRange.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptBlockPosSupplier>> blockPosSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptBlockPosSupplier>>builder()
			.put(StaticBlockPos.nameKey, (p, k) -> new StaticBlockPos(p))
			.put(FromBlockPosVariable.nameKey, (p, k) -> new FromBlockPosVariable(p))
			.put(BlockPosAdd.nameKey, (p, k) -> new BlockPosAdd(p))
			.put(PlayerBlockPos.nameKey, (p, k) -> new PlayerBlockPos(p))
			.build();
	public static final ImmutableMap<String, Object> blockPosSupplierConfigsTree = treeBuilder()
		.put(StaticBlockPos.fullKey)
		.put(FromBlockPosVariable.fullKey)
		.put(BlockPosAdd.fullKey)
		.put(PlayerBlockPos.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptVector3dSupplier>> vector3dSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptVector3dSupplier>>builder()
			.put(StaticVector3d.nameKey, (p, k) -> new StaticVector3d(p))
			.put(FromVector3dVariable.nameKey, (p, k) -> new FromVector3dVariable(p))
			.put(FromBlockPos.nameKey, (p, k) -> new FromBlockPos(p))
			.put(BlockCenterPos.nameKey, (p, k) -> new BlockCenterPos(p))
			.build();
	public static final ImmutableMap<String, Object> vector3dSupplierConfigsTree = treeBuilder()
		.put(StaticVector3d.fullKey)
		.put(FromVector3dVariable.fullKey)
		.put(FromBlockPos.fullKey)
		.put(BlockCenterPos.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptDirection6Supplier>> direction6SupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptDirection6Supplier>>builder()
			.put(StaticDirection6.nameKey, (p, k) -> new StaticDirection6(p))
			.build();
	public static final ImmutableMap<String, Object> direction6SupplierConfigsTree = treeBuilder()
		.put(StaticDirection6.fullKey)
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
