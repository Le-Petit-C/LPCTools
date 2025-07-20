package lpctools.scripts;

import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;

//除ScriptConfig本身以外，你应当保证任何一个IScriptBase的parent也是IScriptBase
public interface IScriptBase extends ILPCUniqueConfigBase {
	default ScriptConfig getScript(){return ((IScriptBase)getParent()).getScript();}
	String fullPrefix = "lpctools.configs.scripts.";
}
