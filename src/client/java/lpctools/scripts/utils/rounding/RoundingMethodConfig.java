package lpctools.scripts.utils.rounding;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.IScriptBase;
import org.jetbrains.annotations.NotNull;

public class RoundingMethodConfig extends ArrayOptionListConfig<RoundingMethod> implements IScriptBase {
	public RoundingMethodConfig(@NotNull ILPCConfigReadable parent) {
		super(parent, nameKey);
		for(RoundingMethod method : RoundingMethod.values())
			addOption(method.fullKey, method);
	}
	@Override public void onValueChanged() {
		notifyScriptChanged();
		super.onValueChanged();
	}
	@Override public void getButtonOptions(ButtonOptionArrayList res) {}
	@Override public @NotNull String getFullTranslationKey() {return fullKey;}
	public static final String nameKey = "roundingMethod";
	public static final String fullKey = IScriptBase.fullPrefix + nameKey;
}
