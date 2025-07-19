package lpctools.scripts;

import com.google.common.collect.ImmutableSortedMap;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;

import static lpctools.lpcfymasaapi.LPCConfigStatics.addMutableConfig;
import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class ScriptConfigs {
    public static final LPCConfigList script = new LPCConfigList(LPCTools.page, "scripts");
    static {listStack.push(script);}
    public static final MutableConfig<ScriptConfig> scripts = addMutableConfig("scripts", script.getFullTranslationKey(), ImmutableSortedMap.of(
        "script", ScriptConfig::new
    ), null);
    static {listStack.pop();}
}
