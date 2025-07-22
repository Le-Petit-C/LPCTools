package lpctools.scripts;

import com.google.common.collect.ImmutableMap;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.runners.IScriptRunner;
import lpctools.scripts.runners.InteractBlock;
import lpctools.scripts.runners.RunnerMessage;
import lpctools.scripts.runners.SubRunners;
import lpctools.scripts.runners.setVariable.SetBlockPosVariable;
import lpctools.scripts.runners.setVariable.SetVariable;
import lpctools.scripts.runners.variables.BlockPosVariable;
import lpctools.scripts.runners.variables.IntVariable;
import lpctools.scripts.runners.variables.Variable;
import lpctools.scripts.suppliers.blockPos.BlockPosAdd;
import lpctools.scripts.suppliers.blockPos.PlayerBlockPos;
import lpctools.scripts.suppliers.blockPos.FromBlockPosVariable;
import lpctools.scripts.suppliers.IScriptSupplier;
import lpctools.scripts.suppliers.blockPos.StaticBlockPos;
import lpctools.scripts.trigger.TriggerHotkey;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public class ScriptConfigData {
	static final ImmutableMap<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>> triggerConfigs =
		ImmutableMap.<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>>builder()
			.put(TriggerHotkey.fullKey, (p, k, r)->new TriggerHotkey(p, r))
			.build();
	public static final ImmutableMap<String, BiFunction<MutableConfig<IScriptRunner>, String, IScriptRunner>> runnerConfigs =
		ImmutableMap.<String, BiFunction<MutableConfig<IScriptRunner>, String, IScriptRunner>>builder()
			.put(BlockPosVariable.fullKey, (p, k)->new BlockPosVariable(p))
			.put(IntVariable.fullKey, (p, k)->new IntVariable(p))
			.put(SetBlockPosVariable.fullKey, (p, k)->new SetBlockPosVariable(p))
			.put(SubRunners.fullKey, (p, k) -> new SubRunners(p))
			.put(InteractBlock.fullKey, (p, k) -> new InteractBlock(p))
			.put(RunnerMessage.fullKey, (p, k) -> new RunnerMessage(p))
			.build();
	public static final ImmutableMap<String, Object> runnerConfigsTree = treeBuilder()
		.put(Variable.fullKey, treeBuilder()
			.put(BlockPosVariable.fullKey)
			.put(IntVariable.fullKey)
			.build())
		.put(SetVariable.fullKey, treeBuilder()
			.put(SetBlockPosVariable.fullKey)
			.build())
		.put(SubRunners.fullKey)
		.put(InteractBlock.fullKey)
		.put(RunnerMessage.fullKey)
		.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptSupplier<BlockPos>>> blockPosSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptSupplier<BlockPos>>>builder()
			.put(StaticBlockPos.fullKey, (p, k) -> new StaticBlockPos(p))
			.put(FromBlockPosVariable.fullKey, (p, k) -> new FromBlockPosVariable(p))
			.put(BlockPosAdd.fullKey, (p, k) -> new BlockPosAdd(p))
			.put(PlayerBlockPos.fullKey, (p, k) -> new PlayerBlockPos(p))
			.build();
	public static final ImmutableMap<String, Object> blockPosSupplierConfigsTree = treeBuilder()
		.put(StaticBlockPos.fullKey)
		.put(FromBlockPosVariable.fullKey)
		.put(BlockPosAdd.fullKey)
		.put(PlayerBlockPos.fullKey)
		.build();
	private static class TreeBuilder extends ImmutableMap.Builder<String, Object>{
		public TreeBuilder put(String k, ImmutableMap<String, Object> subTree){
			super.put(k, subTree);
			return this;
		}
		public TreeBuilder put(String k){
			super.put(k, k);
			return this;
		}
	}
	private static TreeBuilder treeBuilder(){return new TreeBuilder();}
}
