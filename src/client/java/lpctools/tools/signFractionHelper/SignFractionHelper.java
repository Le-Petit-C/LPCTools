package lpctools.tools.signFractionHelper;

import com.google.common.collect.ImmutableMap;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.BooleanConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonHotkeyConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.FractionConfig;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.math.Fraction;

import java.util.Map;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class SignFractionHelper {
	public static final BooleanHotkeyThirdListConfig SFConfig = ToolUtils.configBuilder("SF").withToolRunner(SignFractionHelperRunner::new).build();

	static {listStack.push(SFConfig);}
	public static final FractionConfig selected = addConfigEx(l -> new FractionConfig(l, "selected", Fraction.ZERO));
	public static final FractionConfig iteration = addConfigEx(l -> new FractionConfig(l, "iteration", Fraction.ONE));
	public static final ArrayOptionListConfig<Integer> modRange = addArrayOptionListConfig("modRange", modRangeOptions());
	public static final ButtonHotkeyConfig increase = addButtonHotkeyConfig("increase", null, () -> iterate(true));
	public static final ButtonHotkeyConfig decrease = addButtonHotkeyConfig("decrease", null, () -> iterate(false));
	public static final ButtonHotkeyConfig doubleIteration = addButtonHotkeyConfig("doubleIteration", null, () -> scaleIteration(true));
	public static final ButtonHotkeyConfig halveIteration = addButtonHotkeyConfig("halveIteration", null, () -> scaleIteration(false));
	public static final BooleanConfig alwaysCloseScreen = addBooleanConfig("alwaysCloseScreen", false);
	static {listStack.pop();}

	private static Map<String, Integer> modRangeOptions() {
		return ImmutableMap.<String, Integer>builder()
			.put("lpctools.configs.tools.SF.modRange.two", 2)
			.put("lpctools.configs.tools.SF.modRange.one", 1)
			.build();
	}

	public static boolean isEnabled() { return SFConfig.getBooleanValue(); }

	static void overlayKey(String key) { DataUtils.clientMessage(Component.translatable("lpctools.tools.SF.overlay." + key), true); }
	static void overlayText(String key, Object... args) { DataUtils.clientMessage(Component.translatable("lpctools.tools.SF.overlay." + key, args), true); }

	private static boolean iterate(boolean forward) {
		if(!isEnabled()) return false;
		Fraction current = selected.getFraction(), step = iteration.getFraction();
		Fraction result;
		int range = modRange.get();
		int moddedAmount;
		try {
			Fraction tempResult = forward ? current.add(step) : current.subtract(step);
			int intPart = Math.floorDiv(tempResult.getNumerator(), tempResult.getDenominator());
			moddedAmount = Math.floorDiv(intPart, range) * range;
			result = tempResult.add(Fraction.getFraction(-moddedAmount, 1));
		}
		catch (ArithmeticException e) { overlayKey("overflowValue"); return true; }
		selected.setFraction(result);
		overlayText("iterate", result.toString(),
			current.toString(), forward ? "+" : "-", step.toString(),
			Integer.toString(moddedAmount), result.toString());
		return true;
	}

	private static boolean scaleIteration(boolean expand) {
		if(!isEnabled()) return false;
		Fraction step = iteration.getFraction();
		Fraction result;
		try {
			result = step.multiplyBy(expand ? Fraction.getFraction(2, 1) : Fraction.getFraction(1, 2));
		}
		catch (ArithmeticException e) {
			overlayKey(expand ? "iterationOverflow" : "iterationPrecision");
			return true;
		}
		iteration.setFraction(result);
		overlayText(expand ? "doubleIteration" : "halveIteration", result.toString());
		return true;
	}
}
