package lpctools.scripts.runner.variables;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import lpctools.scripts.runner.IScriptRunner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class Variable<T> extends UniqueStringConfig implements IScriptRunner {
     public Variable(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
          super(parent, nameKey, null, callback);
     }
     @Override public Consumer<CompiledVariableList> compile(VariableMap variableMap) {
          variableMap.put(getNameKey(), this);
          return list->list.add(allocate());
     }
     protected abstract T allocate();
     public abstract String getDescription();
}
