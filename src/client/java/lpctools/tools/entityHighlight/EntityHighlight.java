package lpctools.tools.entityHighlight;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.EntityTypeListConfig;
import lpctools.tools.ToolConfigs;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class EntityHighlight {
	public static final BooleanHotkeyThirdListConfig EHConfig = new BooleanHotkeyThirdListConfig(ToolConfigs.toolConfigs, "EH", null);
	static {listStack.push(EHConfig);}
	public static final EntityTypeListConfig entityList = addConfig(new EntityTypeListConfig(EHConfig, "entityList", "", null));
	static {listStack.pop();}
}
