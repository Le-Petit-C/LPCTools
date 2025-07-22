package lpctools.scripts.runners;

import lpctools.scripts.CompileFailedException;
import lpctools.scripts.IScriptBase;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IScriptRunner extends IScriptBase {
    @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException;
    String fullKey = IScriptBase.fullPrefix + "runners";
    String fullPrefix = fullKey + '.';
}
