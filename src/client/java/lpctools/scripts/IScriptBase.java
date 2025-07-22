package lpctools.scripts;

import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;

public interface IScriptBase extends ILPCUniqueConfigBase {
	default ScriptConfig getScript(){
		ILPCConfigBase script = this;
		while(!(script instanceof ScriptConfig config)) script = script.getParent();
		return config;
	}
	default void notifyScriptChanged(){
		getScript().onValueChanged();
	}
	String fullPrefix = "lpctools.configs.scripts.";
}
