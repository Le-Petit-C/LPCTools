package lpctools.scripts.utils.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers.block.IScriptBlockSupplier;

import static lpctools.scripts.ScriptConfigData.*;

public class BlockSupplierChooser extends ChooseConfig<IScriptBlockSupplier> implements IScriptBase {
	public BlockSupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, blockSupplierConfigs, blockSupplierConfigsTree, callback);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}

