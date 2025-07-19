package lpctools.scripts;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.MutableConfig;
import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.StringThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.scripts.runner.IScriptRunner;
import lpctools.scripts.runner.variables.CompiledVariableList;
import lpctools.scripts.runner.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

import static lpctools.scripts.ScriptConfigData.*;

public class ScriptConfig extends StringThirdListConfig {
    @SuppressWarnings("unused")
    public final MutableConfig<ILPCUniqueConfigBase> triggers = addConfig(new MutableConfig<>(this, "triggers", getFullTranslationKey(), triggerConfigs, null, this::runScript));
    public final MutableConfig<IScriptRunner> runner = addConfig(new MutableConfig<>(this, "runner", getFullTranslationKey(), runnerConfigs, null));
    
    public ScriptConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {super(parent, nameKey, null, null);}
    public void runScript(){
        if(scriptRunnable == null) try{
            scriptRunnable = compile();
            scriptRunnable.run();
        } catch (CompileFailedException e){e.putMessage(false);}
        else scriptRunnable.run();
    }
    @Override public void onValueChanged() {
        super.onValueChanged();
        scriptRunnable = null;
    }
    private Runnable scriptRunnable;
    private Runnable compile() throws CompileFailedException{
        ArrayList<Consumer<CompiledVariableList>> compiledList = new ArrayList<>();
        VariableMap map = new VariableMap();
        for(IScriptRunner runnable : runner.iterateConfigs())
            compiledList.add(runnable.compile(map));
        return ()->{
            CompiledVariableList variableList = new CompiledVariableList();
            compiledList.forEach(consumer->consumer.accept(variableList));
        };
    }
}
