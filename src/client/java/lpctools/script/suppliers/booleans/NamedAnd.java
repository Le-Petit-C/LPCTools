package lpctools.script.suppliers.booleans;

import lpctools.script.IScriptWithSubScript;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class NamedAnd extends And {
	protected Text name;
	public NamedAnd(IScriptWithSubScript parent, Text name) {
		super(parent);
		this.name = name;
	}
	@Override @Nullable public Text getName() {
		if(name != null) return name;
		else return super.getName();
	}
}
