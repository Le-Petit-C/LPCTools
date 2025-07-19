package lpctools.scripts;

import fi.dy.masa.malilib.util.StringUtils;
import lpctools.scripts.runner.variables.Variable;

import static lpctools.util.DataUtils.*;

public class CompileFailedException extends Exception {
    public CompileFailedException(String message) {super(message);}
    public void putMessage(boolean overlay){notifyPlayer(getMessage(), overlay);}
    public static CompileFailedException undefinedVariable(String name){
        return new CompileFailedException(StringUtils.translate("lpctools.configs.scripts.exceptions.undefinedVariable", name));
    }
    public static CompileFailedException notExpectedType(String name, Variable<?> foundVariable, String expectedVariableDescription){
        return new CompileFailedException(StringUtils.translate("lpctools.configs.scripts.exceptions.notExpectedType",
            expectedVariableDescription, foundVariable.getDescription(), name));
    }
}
