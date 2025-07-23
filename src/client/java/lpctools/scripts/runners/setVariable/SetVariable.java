package lpctools.scripts.runners.setVariable;

import fi.dy.masa.malilib.gui.LeftRight;
import fi.dy.masa.malilib.gui.MaLiLibIcons;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ChooseConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.StringThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runners.IScriptRunner;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import lpctools.scripts.runners.variables.VariableTestPack;
import lpctools.scripts.suppliers.IScriptSupplier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class SetVariable<T extends IScriptSupplier<?>> extends StringThirdListConfig implements IScriptRunner {
	public final ChooseConfig<T> supplier;
	public SetVariable(@NotNull ILPCConfigReadable parent, String nameKey, ChooseConfig<T> supplier) {
		super(parent, nameKey, null, null);
		setValueChangeCallback(this::notifyScriptChanged);
		this.supplier = supplier;
		addConfig(supplier.get());
		supplier.setValueChangeCallback(()->{
			getConfigs().clear();
			addConfig(supplier.get());
		});
	}
	@Override public @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException{
		int index = variableMap.get(getStringValue(), testPack());
		return setValue(variableMap, supplier.get(), index);
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		super.getButtonOptions(res);
		res.add(-1, (button, mouseButton)->supplier.openChoose(), null, ILPCUniqueConfigBase.iconButtonAllocator(MaLiLibIcons.SEARCH, LeftRight.CENTER));
	}
	@Override public void setAlignedIndent(int indent) {supplier.setAlignedIndent(indent);}
	@Override public int getAlignedIndent() {return supplier.getAlignedIndent();}
	
	protected abstract VariableTestPack testPack();
	protected abstract @NotNull Consumer<CompiledVariableList>
	setValue(VariableMap variableMap, T src, int index) throws CompileFailedException;
	public static final String nameKey = "setVariable";
	public static final String fullKey = IScriptRunner.fullPrefix + nameKey;
	public static final String fullPrefix = fullKey + '.';
}
