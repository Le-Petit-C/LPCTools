package lpctools.scripts.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers._double.IScriptDoubleSupplier;

import static lpctools.scripts.ScriptConfigData.*;

public class DoubleSupplierChooser extends ChooseConfig<IScriptDoubleSupplier> implements IScriptBase {
	public DoubleSupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, doubleSupplierConfigs, doubleSupplierConfigsTree, callback);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}

