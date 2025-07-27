package lpctools.scripts.suppliers._boolean.comparator;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.suppliers._boolean.IScriptBooleanSupplier;
import org.jetbrains.annotations.NotNull;

import static lpctools.scripts.suppliers._boolean.comparator.Comparators.*;

public class AllComparatorConfig extends ArrayOptionListConfig<IAllComparable> implements IScriptBase {
	public AllComparatorConfig(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey);
		for(IAllComparable comparable : allComparable)
			addOption(comparable.key(), comparable);
	}
	
	@Override public void onValueChanged() {
		notifyScriptChanged();
		super.onValueChanged();
	}
	
	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		/*res.add(1, (button, mouseButton)->{
			switch (mouseButton){
				case 0 -> setOptionListValue(getOptionListValue().cycle(true));
				case 1 -> setOptionListValue(getOptionListValue().cycle(false));
			}
		}, get()::key, buttonGenericAllocator);*/
	}
	
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "comparator";
	public static final String fullKey = IScriptBooleanSupplier.fullPrefix + nameKey;
}
