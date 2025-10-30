package lpctools.script;

import net.minecraft.text.Text;

public class ScriptCompileException extends Exception {
	public ScriptCompileException(String message) {super(message);}
	public ScriptCompileException ofUndefined(String targetVariable){
		return new ScriptCompileException(Text.translatable("lpctools.script.exception.compile.undefinedVariable").getString() + targetVariable);
	}
}
