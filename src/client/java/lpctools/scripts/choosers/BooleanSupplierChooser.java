package lpctools.scripts.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers._boolean.IScriptBooleanSupplier;

import static lpctools.scripts.ScriptConfigData.*;

public class BooleanSupplierChooser extends ChooseConfig<IScriptBooleanSupplier> implements IScriptBase {
	public BooleanSupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, booleanSupplierConfigs, booleanSupplierConfigsTree, callback);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}

