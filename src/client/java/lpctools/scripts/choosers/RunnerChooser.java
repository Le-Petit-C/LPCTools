package lpctools.scripts.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.runners.IScriptRunner;

import static lpctools.scripts.ScriptConfigData.*;

public class RunnerChooser extends ChooseConfig<IScriptRunner> implements IScriptBase {
	public RunnerChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, runnerConfigs, runnerConfigsTree, callback);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}
