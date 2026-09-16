package lpctools.tools.signFractionHelper;

import com.google.common.collect.ImmutableMap;
import lpctools.lpcfymasaapi.configButtons.derivedConfigs.ArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.ButtonHotkeyConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.FractionConfig;
import lpctools.tools.ToolUtils;
import lpctools.util.DataUtils;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.math.Fraction;

import java.util.Map;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

/**
 * 告示牌分数辅助：打开告示牌编辑界面时把「选定值」写进正面第一行并跳过界面，
 * 省掉"开界面 → 输入 → 回车"三步；再用四个按钮/热键迭代这个值，边建边贴很顺手。
 * <p>
 * 这个工具只负责"往告示牌上写分数"，分数代表什么由别的功能（甚至别的 mod）决定。
 * <p>
 * 数值一律用 commons-lang3 的 {@link Fraction}（MC 自带库）表示：它算术时自动约分，
 * 且<b>溢出直接抛 ArithmeticException</b>——正好就是我们要的"拒绝"信号，
 * 所以不用自己写溢出判断，也绝不会静默把值近似掉。
 * <p>
 * 迭代改动只留在内存里，<b>有意不在这些热键里 {@code save()}</b>：等配置整体存盘时一起落地。
 * <p>
 * 换向/边界规则：
 * <ul>
 *     <li>加减的结果一律按 range 的整数倍折回 {@code [0, range)}（range 可配 1 或 2），负数也能正确绕回</li>
 *     <li>任何一步放不进 int → 原样拒绝并提示，值保持不变</li>
 *     <li>迭代值可以超出取模范围（{@code ×2} 不受范围限制），加减时会按范围正常折回</li>
 *     <li>{@code ÷2} 到达精度下限（分母超 int）时拒绝而不是近似：近似会把这个方向的值直接毁掉</li>
 * </ul>
 */
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
			// 越界就拒绝：÷2 做近似会把值压成 0，×2 做近似也会让精度对不上肉眼预期
			overlayKey(expand ? "iterationOverflow" : "iterationPrecision");
			return true;
		}
		iteration.setFraction(result);
		overlayText(expand ? "doubleIteration" : "halveIteration", result.toString());
		return true;
	}
}
