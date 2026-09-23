package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.config.IStringRepresentable;
import lpctools.lpcfymasaapi.configButtons.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.apache.commons.lang3.math.Fraction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FractionConfig extends LPCUniqueConfigBase implements IStringRepresentable, IConfigResettable {
	public final Fraction defaultFraction;
	Fraction fraction;
	public FractionConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Fraction defaultFraction, @Nullable ILPCValueChangeCallback callback) {
		super(parent, nameKey, callback);
		fraction = this.defaultFraction = defaultFraction;
	}

	public FractionConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, Fraction defaultFraction) { this(parent, nameKey, defaultFraction, null); }

	public Fraction getDefaultFraction() { return defaultFraction; }
	public Fraction getFraction() { return fraction; }
	public void setFraction(Fraction fraction) {
		if(fraction.equals(this.fraction)) return;
		this.fraction = fraction;
		onValueChanged();
	}

	@Override public void getButtonOptions(ButtonOptionArrayList res) {
		res.add(ILPCUniqueConfigBase.textFieldPreset(1, this));
	}

	@Override public @Nullable JsonElement getAsJsonElement() { return new JsonPrimitive(getStringValue()); }
	@Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
		if (element instanceof JsonPrimitive primitive
			&& parse(primitive.getAsString()) instanceof Fraction f
			&& !f.equals(fraction)) {
			fraction = f;
			return new UpdateTodo().valueChanged();
		}
		return new UpdateTodo();
	}

	@Override public String getDefaultStringValue() { return defaultFraction.toString(); }

	private static @Nullable Fraction parse(String s) {
		try { return Fraction.getFraction(s.trim()).reduce(); }
		catch (NumberFormatException | ArithmeticException _) { return null; }
	}

	@Override public void setValueFromString(String s) {
		if(parse(s) instanceof Fraction f) setFraction(f);
	}

	@Override public boolean isModified(String s) {
		return !(parse(s) instanceof Fraction f) || !f.equals(defaultFraction);
	}

	@Override public String getStringValue() { return fraction.toString(); }
	@Override public boolean isModified() { return !defaultFraction.equals(fraction); }
	@Override public void resetToDefault() { setFraction(defaultFraction); }
}
