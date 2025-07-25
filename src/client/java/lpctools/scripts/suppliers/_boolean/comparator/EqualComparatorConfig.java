package lpctools.scripts.suppliers._boolean.comparator;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers._boolean.IScriptBooleanSupplier;
import org.jetbrains.annotations.NotNull;

import static lpctools.scripts.suppliers._boolean.comparator.Comparators.*;

public class EqualComparatorConfig extends ArrayOptionListConfig<IEqualComparable> implements IScriptBase {
	public EqualComparatorConfig(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey);
		for(IEqualComparable comparable : equalComparable)
			addOption(comparable.key(), comparable);
	}
	
	@Override public void onValueChanged() {
		notifyScriptChanged();
		super.onValueChanged();
	}
	
	@Override public void getButtonOptions(ILPCUniqueConfigBase.ButtonOptionArrayList res) {
		res.add(1, (button, mouseButton)->{
			switch (mouseButton){
				case 0 -> setOptionListValue(getOptionListValue().cycle(true));
				case 1 -> setOptionListValue(getOptionListValue().cycle(false));
			}
		}, get()::key, buttonGenericAllocator);
	}
	
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "comparator";
	public static final String fullKey = IScriptBooleanSupplier.fullPrefix + nameKey;
}
