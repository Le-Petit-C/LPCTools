package lpctools.scripts;

import lpctools.LPCTools;
import lpctools.lpcfymasaapi.LPCConfigList;

import static lpctools.lpcfymasaapi.LPCConfigStatics.listStack;

public class ScriptConfigs {
    public static final LPCConfigList script = new LPCConfigList(LPCTools.page, "scripts");
    static {listStack.push(script);}
    //public static final MutableConfig<ScriptConfig> scripts = addMutableConfig("scripts",)
    static {listStack.pop();}
}
