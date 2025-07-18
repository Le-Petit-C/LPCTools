package lpctools.scripts.variables.basic;

import com.google.gson.JsonElement;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.LPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ScriptConfigurableInteger extends LPCUniqueConfigBase implements IntVariable {
    int value;
    public ScriptConfigurableInteger(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
    }
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
    
    }
    
    @Override
    public @Nullable JsonElement getAsJsonElement() {
        return null;
    }
    
    @Override
    public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        return null;
    }
    @Override public void setAsInt(int value) {
    
    }
    @Override public int getAsInt() {
        return 0;
    }
}
