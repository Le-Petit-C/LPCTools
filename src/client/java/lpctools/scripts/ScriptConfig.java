package lpctools.scripts;

import com.google.gson.JsonElement;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.configbutton.uniqueConfigs.LPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScriptConfig extends LPCUniqueConfigBase {
    public ScriptConfig(@NotNull ILPCConfigReadable parent, @NotNull String nameKey, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
    }
    
    @Override
    public void getButtonOptions(ButtonOptionArrayList res) {
    
    }
    
    @Override
    public @Nullable JsonElement getAsJsonElement() {
        return null;
    }
    
    @Override
    public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement element) {
        return null;
    }
}
