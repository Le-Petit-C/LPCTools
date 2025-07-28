package lpctools.scripts.utils.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers.vector3d.IScriptVector3dSupplier;

import static lpctools.scripts.ScriptConfigData.*;

public class Vector3dSupplierChooser extends ChooseConfig<IScriptVector3dSupplier> implements IScriptBase {
	public Vector3dSupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, vector3dSupplierConfigs, vector3dSupplierConfigsTree, callback);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}

