package lpctools.scripts.utils.operators;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.IScriptBase;
import org.jetbrains.annotations.NotNull;

import static lpctools.scripts.utils.operators.Operators.*;

public class ExtraOperatorConfig extends ArrayOptionListConfig<IExtraOperator> implements IScriptBase {
	public ExtraOperatorConfig(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey);
		for(IExtraOperator operator : extraOperators)
			addOption(operator.key(), operator);
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
	public static final String nameKey = "operator";
	public static final String fullKey = IScriptBase.fullPrefix + nameKey;
}
