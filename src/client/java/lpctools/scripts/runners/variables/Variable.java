package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.IScriptRunner;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class Variable<T> extends UniqueStringConfig implements IScriptRunner {
     public Variable(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {
          super(parent, nameKey, null, null);
          setValueChangeCallback(()->getScript().onValueChanged());
     }
     @Override public @NotNull Consumer<CompiledVariableList> compile(VariableMap variableMap) {
          variableMap.put(getStringValue(), this);
          return list->list.add(allocate());
     }
     protected abstract T allocate();
     public static final String nameKey = "variables";
     public static final String fullKey = IScriptRunner.fullPrefix + nameKey;
     public static final String fullPrefix = fullKey + '.';
}
