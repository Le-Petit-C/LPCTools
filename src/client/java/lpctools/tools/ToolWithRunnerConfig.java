package lpctools.tools;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.util.DataUtils;
import lpctools.util.javaex.QuietAutoCloseable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class ToolWithRunnerConfig<T extends ToolUtils.ToolRunner> extends BooleanHotkeyThirdListConfig {
	private final ToolUtils.ToolRunnerSupplier<T> toolRunnerSupplier;
	private @Nullable T runner;
	public ToolWithRunnerConfig(ILPCConfigList parent, @NotNull String nameKey, ToolUtils.ToolRunnerSupplier<T> toolRunnerSupplier, ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		this.toolRunnerSupplier = toolRunnerSupplier;
	}
	public @Nullable T getRunner() { return runner; }
	public void applyToRunnerIfPresent(Consumer<? super T> action) { if(runner != null) action.accept(runner); }
	public ILPCValueChangeCallback runnerApplyCallback(Consumer<? super T> action) { return ()->applyToRunnerIfPresent(action); }
	@Override public void onValueChanged() {
		if (getBooleanValue()) {
			if(runner == null) {
				try {
					runner = toolRunnerSupplier.createRunner();
				} catch (ToolUtils.RunnerCreateFailedException e) {
					setBooleanValue(false);
					DataUtils.clientMessage(e.failReason, true);
					return;
				}
			}
			runner.registerAll(true);
		}
		else {
			if(runner != null) {
				runner.registerAll(false);
				if(runner instanceof QuietAutoCloseable closeable)
					closeable.close();
				else if(runner instanceof AutoCloseable closeable) {
					try {
						closeable.close();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
				runner = null;
			}
		}
		super.onValueChanged();
	}
}
