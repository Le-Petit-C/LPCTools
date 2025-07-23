package lpctools.scripts;

import com.google.common.collect.ImmutableMap;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.runners.*;
import lpctools.scripts.runners.setVariable.SetBlockPosVariable;
import lpctools.scripts.runners.setVariable.SetBooleanVariable;
import lpctools.scripts.runners.setVariable.SetIntVariable;
import lpctools.scripts.runners.setVariable.SetVariable;
import lpctools.scripts.runners.variables.BlockPosVariable;
import lpctools.scripts.runners.variables.BooleanVariable;
import lpctools.scripts.runners.variables.IntVariable;
import lpctools.scripts.runners.variables.Variable;
import lpctools.scripts.suppliers._boolean.BlockPosEquals;
import lpctools.scripts.suppliers._boolean.FromBooleanVariable;
import lpctools.scripts.suppliers._boolean.IScriptBooleanSupplier;
import lpctools.scripts.suppliers._boolean.StaticBoolean;
import lpctools.scripts.suppliers._int.FromIntVariable;
import lpctools.scripts.suppliers._int.IScriptIntSupplier;
import lpctools.scripts.suppliers._int.StaticInt;
import lpctools.scripts.suppliers.blockPos.*;
import lpctools.scripts.trigger.TriggerHotkey;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public class ScriptConfigData {
	static final ImmutableMap<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>> triggerConfigs =
		ImmutableMap.<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>>builder()
			.put(TriggerHotkey.fullKey, (p, k, r)->new TriggerHotkey(p, r))
			.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptRunner>> runnerConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptRunner>>builder()
			.put(DoNothing.nameKey, (p, k)->new DoNothing(p))
			.put(BooleanVariable.nameKey, (p, k)->new BooleanVariable(p))
			.put(IntVariable.nameKey, (p, k)->new IntVariable(p))
			.put(BlockPosVariable.nameKey, (p, k)->new BlockPosVariable(p))
			.put(SetBooleanVariable.nameKey, (p, k)->new SetBooleanVariable(p))
			.put(SetIntVariable.nameKey, (p, k)->new SetIntVariable(p))
			.put(SetBlockPosVariable.nameKey, (p, k)->new SetBlockPosVariable(p))
			.put(SubRunners.nameKey, (p, k)->new SubRunners(p))
			.put(RunIfElse.nameKey, (p, k)->new RunIfElse(p))
			.put(InteractBlock.nameKey, (p, k)->new InteractBlock(p))
			.put(RunnerMessage.nameKey, (p, k)->new RunnerMessage(p))
			.build();
	public static final ImmutableMap<String, Object> runnerConfigsTree = treeBuilder()
		.put(DoNothing.fullKey)
		.put(Variable.fullKey, treeBuilder()
			.put(BooleanVariable.fullKey)
			.put(IntVariable.fullKey)
			.put(BlockPosVariable.fullKey)
			.build())
		.put(SetVariable.fullKey, treeBuilder()
			.put(SetBooleanVariable.fullKey)
			.put(SetIntVariable.fullKey)
			.put(SetBlockPosVariable.fullKey)
			.build())
		.put(RunIfElse.fullKey)
		.put(SubRunners.fullKey)
		.put(InteractBlock.fullKey)
		.put(RunnerMessage.fullKey)
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
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptBooleanSupplier>> booleanSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptBooleanSupplier>>builder()
			.put(StaticBoolean.nameKey, (p, k) -> new StaticBoolean(p))
			.put(FromBooleanVariable.nameKey, (p, k)->new FromBooleanVariable(p))
			.put(BlockPosEquals.nameKey, (p, k)->new BlockPosEquals(p))
			.build();
	public static final ImmutableMap<String, Object> booleanSupplierConfigsTree = treeBuilder()
		.put(StaticBoolean.fullKey)
		.put(FromBooleanVariable.fullKey)
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
