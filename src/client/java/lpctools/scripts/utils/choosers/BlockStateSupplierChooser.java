package lpctools.scripts.utils.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers.blockState.IScriptBlockStateSupplier;

import static lpctools.scripts.ScriptConfigData.*;

public class BlockStateSupplierChooser extends ChooseConfig<IScriptBlockStateSupplier> implements IScriptBase {
	public BlockStateSupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, blockStateSupplierConfigs, blockStateSupplierConfigsTree, callback);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}

