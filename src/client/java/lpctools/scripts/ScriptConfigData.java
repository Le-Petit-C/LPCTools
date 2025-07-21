package lpctools.scripts;

import com.google.common.collect.ImmutableMap;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.runners.IScriptRunner;
import lpctools.scripts.runners.InteractBlock;
import lpctools.scripts.runners.RunnerMessage;
import lpctools.scripts.runners.SubRunners;
import lpctools.scripts.suppliers.blockPos.PlayerBlockPos;
import lpctools.scripts.suppliers.interfaces.IScriptSupplier;
import lpctools.scripts.suppliers.staticSuppliers.StaticBlockPos;
import lpctools.scripts.trigger.TriggerHotkey;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public class ScriptConfigData {
	static final ImmutableMap<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>> triggerConfigs =
		ImmutableMap.<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>>builder()
			.put(TriggerHotkey.nameKey, (p, k, r)->new TriggerHotkey(p, r))
			.build();
	public static final ImmutableMap<String, BiFunction<MutableConfig<IScriptRunner>, String, IScriptRunner>> runnerConfigs =
		ImmutableMap.<String, BiFunction<MutableConfig<IScriptRunner>, String, IScriptRunner>>builder()
			.put(SubRunners.nameKey, (p, k) -> new SubRunners(p))
			.put(InteractBlock.nameKey, (p, k) -> new InteractBlock(p))
			.put(RunnerMessage.nameKey, (p, k) -> new RunnerMessage(p))
			.build();
	public static final ImmutableMap<String, BiFunction<ILPCConfigReadable, String, IScriptSupplier<BlockPos>>> blockPosSupplierConfigs =
		ImmutableMap.<String, BiFunction<ILPCConfigReadable, String, IScriptSupplier<BlockPos>>>builder()
			.put(StaticBlockPos.nameKey, (p, k) -> new StaticBlockPos(p))
			.put(PlayerBlockPos.nameKey, (p, k) -> new PlayerBlockPos(p))
			.build();
}
