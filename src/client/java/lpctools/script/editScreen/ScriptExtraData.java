package lpctools.script.editScreen;

import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.IScript;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static lpctools.lpcfymasaapi.LPCConfigUtils.calculateTextButtonWidth;

class ScriptExtraData {
	private final ScriptEditScreen scriptEditScreen;
	final @NotNull IScript script;
	final @Nullable GuidelineExpandButton expandButton;
	int lines = -1;
	final @NotNull ButtonGeneric nameButton;
	final @NotNull ScriptExtraOptionButton extraOptionButton;
	
	ScriptExtraData(IScript script) {
		this.scriptEditScreen = script.getScript().editScreen;
		this.script = script;
		String name = script.getName();
		nameButton = new ButtonGeneric(0, 0, calculateTextButtonWidth(name, scriptEditScreen.textRenderer, 20), 20, name)
			.setRenderDefaultBackground(false);
		if (script.getSubScripts() != null) {
			expandButton = new GuidelineExpandButton(scriptEditScreen, script);
			nameButton.setActionListener((button, mouseButton) -> toggleExtended());
		} else expandButton = null;
		extraOptionButton = new ScriptExtraOptionButton(scriptEditScreen, script);
	}
	
	int getLines() {return lines > 0 ? lines : 1;}
	
	void toggleExtended() {
		if (lines > 0) lines = -1;
		else //if(script.getSubScripts() instanceof Iterable<? extends IScript> subScripts){
			lines = 1;
		//for(var script : subScripts)
		//	lines += getExtraData(script).getLines();
		//}
		scriptEditScreen.markNeedUpdate();
	}
	
	boolean extended() {return lines > 0;}
}
