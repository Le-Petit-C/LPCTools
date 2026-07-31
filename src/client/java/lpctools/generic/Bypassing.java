package lpctools.generic;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.EnumArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.util.inGame.BlockBreakingBypassMethod;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class Bypassing {
	public static final BooleanThirdListConfig bypassing = new BooleanThirdListConfig(GenericConfigs.generic, "bypassing", false, null);
	static { listStack.push(bypassing); }
	public static final BooleanThirdListConfig restrictRotateSpeed = addBooleanThirdListConfig("restrictRotateSpeed", false, null);
	public static final DoubleConfig maxRotateSpeed = addDoubleConfig(restrictRotateSpeed, "maxRotateSpeed", 0.5);
	public static final EnumArrayOptionListConfig<BlockBreakingBypassMethod> blockBreakingBypass = addConfigEx(l-> new EnumArrayOptionListConfig<>(l, "blockBreakingBypass", BlockBreakingBypassMethod.class));
	static { listStack.pop(); }
}
