package lpctools.scripts;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.StringThirdListConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.scripts.runners.SubRunners;
import lpctools.scripts.runners.variables.CompiledVariableList;
import lpctools.scripts.runners.variables.VariableMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ScriptConfig extends StringThirdListConfig implements IScriptBase {
    @SuppressWarnings("unused")
    public final TriggerConfig triggers = addConfig(new TriggerConfig(this));
    public final SubRunners runners = addConfig(new SubRunners(this));
    
    public ScriptConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey) {
        super(parent, nameKey, null, null);
    }
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
        Consumer<CompiledVariableList> runner = runners.compile(new VariableMap());
        return ()->runner.accept(new CompiledVariableList());
    }
    @Override public ScriptConfig getScript() {return this;}
}
