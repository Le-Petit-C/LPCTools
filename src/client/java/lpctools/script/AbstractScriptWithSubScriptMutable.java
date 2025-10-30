package lpctools.script;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import lpctools.script.editScreen.ScriptWithSubScriptMutableDisplayWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class AbstractScriptWithSubScriptMutable<T extends IScript> implements IScriptWithSubScriptMutable<T> {
	protected @Nullable ScriptWithSubScriptMutableDisplayWidget<T> displayWidget;
	protected final ArrayList<T> subScripts = new ArrayList<>();
	
	public final IScriptWithSubScript parent;
	public AbstractScriptWithSubScriptMutable(IScriptWithSubScript parent){
		this.parent = parent;
	}
	
	@Override @Nullable public IScriptWithSubScript getParent() {
		return parent;
	}
	
	@Override public @NotNull ScriptWithSubScriptMutableDisplayWidget<T> getDisplayWidget() {
		if(displayWidget == null) displayWidget = new ScriptWithSubScriptMutableDisplayWidget<>(this);
		return displayWidget;
	}
	@Override @NotNull public ArrayList<T> getSubScripts() {return subScripts;}
	
	public ButtonBase createAddButton(){
		return new ButtonGeneric(0, 0, 20, 20, "+").setActionListener(
			(button, mouseButton) -> notifyInsertion(option->{
				getSubScripts().add(option);
				getDisplayWidget().markUpdateChain();
			})
		);
	}
}
