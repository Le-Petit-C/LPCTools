package lpctools.scripts.runners.variables;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueStringConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigBase;
import lpctools.scripts.runners.IScriptRunner;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class Variable<T> extends UniqueStringConfig implements IScriptRunner {
     public Variable(@NotNull ILPCConfigBase parent, @NotNull String nameKey) {
          super(parent, nameKey, null, null);
          setValueChangeCallback(()->getScript().onValueChanged());
     }
     @Override public Consumer<CompiledVariableList> compile(VariableMap variableMap) {
          variableMap.put(getNameKey(), this);
          return list->list.add(allocate());
     }
     @Override public @NotNull String getFullTranslationKey() {
          return "lpctools.configs.scripts.runners.variables." + getNameKey();
     }
     protected abstract Mutable<T> allocate();
}
