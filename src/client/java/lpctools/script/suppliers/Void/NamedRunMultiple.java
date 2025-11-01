package lpctools.script.suppliers.Void;

import lpctools.script.IScriptWithSubScript;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class NamedRunMultiple extends RunMultiple {
	protected Text name;
	public NamedRunMultiple(IScriptWithSubScript parent, Text name) {
		super(parent);
		this.name = name;
	}
	@Override @Nullable public Text getName() {
		if(name != null) return name;
		else return super.getName();
	}
}
