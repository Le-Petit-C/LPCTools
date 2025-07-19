package lpctools.scripts.runner;

import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.CompileFailedException;
import lpctools.scripts.runner.variables.CompiledVariableList;
import lpctools.scripts.runner.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface IScriptRunner extends ILPCUniqueConfigBase{
    Consumer<CompiledVariableList> compile(VariableMap variableMap) throws CompileFailedException;
    @Override default @NotNull String getFullTranslationKey() {
        return "lpctools.configs.scripts.runners." + getNameKey();
    }
}
