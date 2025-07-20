package lpctools.scripts.runners;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;

import java.util.function.Consumer;

public interface IScriptRunner extends IScriptBase {
    Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException;
    String fullPrefix = IScriptBase.fullPrefix + "runners.";
}
