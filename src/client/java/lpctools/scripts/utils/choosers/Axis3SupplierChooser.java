package lpctools.scripts.utils.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers.axis3.IScriptAxis3Supplier;

import static lpctools.scripts.ScriptConfigData.*;

public class Axis3SupplierChooser extends ChooseConfig<IScriptAxis3Supplier> implements IScriptBase {
	public Axis3SupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, axis3SupplierConfigs, axis3SupplierConfigsTree, callback);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}

