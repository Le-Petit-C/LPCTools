package lpctools.lpcfymasaapi.configButtons.derivedConfigs;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnumArrayOptionListConfig<T extends Enum<T>> extends ArrayOptionListConfig<T> {
	public EnumArrayOptionListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Class<T> clazz) {
		this(parent, nameKey, clazz, null);
	}

	public EnumArrayOptionListConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Class<T> clazz, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		for(T val : clazz.getEnumConstants())
			addOption(val.name().toLowerCase(), val);
	}
}
