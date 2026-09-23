package lpctools.generic;

import lpctools.lpcfymasaapi.configButtons.derivedConfigs.EnumArrayOptionListConfig;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanThirdListConfig;
import lpctools.util.inGame.BlockBreakBypassMethod;
import lpctools.util.inGame.BlockInteractBypassMethod;
import lpctools.util.inGame.BlockPlaceBypassMethod;
import lpctools.util.inGame.EntityBypassMethod;

import static lpctools.lpcfymasaapi.LPCConfigStatics.*;

public class Bypassing {
	public static final BooleanThirdListConfig bypassing = new BooleanThirdListConfig(GenericConfigs.generic, "bypassing", false, null);
	static { listStack.push(bypassing); }
	public static final BooleanThirdListConfig restrictRotateSpeed = addBooleanThirdListConfig("restrictRotateSpeed", false, null);
	public static final DoubleConfig maxRotateSpeed = addDoubleConfig(restrictRotateSpeed, "maxRotateSpeed", 0.5);
	public static final EnumArrayOptionListConfig<BlockBreakBypassMethod> blockBreakBypass = addConfigEx(l-> new EnumArrayOptionListConfig<>(l, "blockBreakBypass", BlockBreakBypassMethod.class));
	public static final EnumArrayOptionListConfig<BlockInteractBypassMethod> blockInteractBypass = addConfigEx(l-> new EnumArrayOptionListConfig<>(l, "blockInteractBypass", BlockInteractBypassMethod.class));
	public static final EnumArrayOptionListConfig<BlockPlaceBypassMethod> blockPlaceBypass = addConfigEx(l-> new EnumArrayOptionListConfig<>(l, "blockPlaceBypass", BlockPlaceBypassMethod.class));
	public static final EnumArrayOptionListConfig<EntityBypassMethod> entityInteractBypass = addConfigEx(l-> new EnumArrayOptionListConfig<>(l, "entityInteractBypass", EntityBypassMethod.class));
	public static final EnumArrayOptionListConfig<EntityBypassMethod> entityAttackBypass = addConfigEx(l-> new EnumArrayOptionListConfig<>(l, "entityAttackBypass", EntityBypassMethod.class));
	static { listStack.pop(); }
}
