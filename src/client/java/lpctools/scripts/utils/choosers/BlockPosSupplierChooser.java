package lpctools.scripts.utils.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers.blockPos.IScriptBlockPosSupplier;

import static lpctools.scripts.ScriptConfigData.*;

public class BlockPosSupplierChooser extends ChooseConfig<IScriptBlockPosSupplier> implements IScriptBase {
	public BlockPosSupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, blockPosSupplierConfigs, blockPosSupplierConfigsTree, callback);
		setValueChangeCallback(this::notifyScriptChanged);
	}
	@Override public void onValueChanged() {
		super.onValueChanged();
		notifyScriptChanged();
	}
}

