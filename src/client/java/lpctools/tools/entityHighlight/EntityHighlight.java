package lpctools.tools.entityHighlight;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.BooleanHotkeyThirdListConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.EntityTypeListConfig;
import lpctools.tools.ToolUtils;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class EntityHighlight {
	public static final BooleanHotkeyThirdListConfig EHConfig = ToolUtils.configBuilder("EH").build();
	static {listStack.push(EHConfig);}
	public static final EntityTypeListConfig entityList = addConfig(new EntityTypeListConfig(EHConfig, "entityList", null));
	static {listStack.pop();}
}
