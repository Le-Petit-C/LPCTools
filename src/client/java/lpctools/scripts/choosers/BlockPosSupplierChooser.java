package lpctools.scripts.choosers;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.ScriptConfigData;
import lpctools.scripts.suppliers.IScriptSupplier;
import net.minecraft.util.math.BlockPos;

import static lpctools.scripts.ScriptConfigData.blockPosSupplierConfigsTree;

public class BlockPosSupplierChooser extends ChooseConfig<IScriptSupplier<BlockPos>> {
	public BlockPosSupplierChooser(ILPCConfigReadable parent, String nameKey, ILPCValueChangeCallback callback) {
		super(parent, nameKey, ScriptConfigData.blockPosSupplierConfigs, blockPosSupplierConfigsTree, callback);
	}
}

