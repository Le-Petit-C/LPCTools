package lpctools.script.editScreen;
import lpctools.script.IScript;
import lpctools.script.IScriptWithSubScriptMutable;

public class ScriptWithSubScriptMutableDisplayWidget<T extends IScript> extends ScriptWithSubScriptDisplayWidget{
	public ScriptWithSubScriptMutableDisplayWidget(IScriptWithSubScriptMutable<T> script) {super(script);}
	@Override public IScriptWithSubScriptMutable<T> getIScript() {
		//noinspection unchecked
		return (IScriptWithSubScriptMutable<T>)super.getIScript();
	}
}
