package lpctools.scripts;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.runner.IScriptRunner;
import lpctools.scripts.runner.RunnerMessage;
import lpctools.scripts.trigger.TriggerHotkey;
import org.apache.commons.lang3.function.TriFunction;

import java.util.function.BiFunction;

public class ScriptConfigData {
	static final ImmutableMap<String, TriFunction<MutableConfig<ILPCUniqueConfigBase>, String, Runnable, ILPCUniqueConfigBase>> triggerConfigs = ImmutableSortedMap.of(
		"hotkey", TriggerHotkey::new
	);
	public static final ImmutableMap<String, BiFunction<MutableConfig<IScriptRunner>, String, IScriptRunner>> runnerConfigs = ImmutableSortedMap.of(
		"message", RunnerMessage::new
	);
}
