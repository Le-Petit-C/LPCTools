package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import com.google.gson.JsonElement;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import lpctools.lpcfymasaapi.configbutton.UpdateTodo;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ButtonConfig extends LPCUniqueConfigBase implements IButtonActionListener {
    @Nullable public IButtonActionListener listener;
    @Nullable public String buttonName;
    public ButtonConfig(ILPCConfigReadable parent, String nameKey, @Nullable IButtonActionListener listener) {
        super(parent, nameKey, null);
        this.listener = listener;
        buttonName = getFullTitleTranslationKey();
    }
    public ButtonConfig(ILPCConfigReadable parent, String nameKey) {this(parent, nameKey, null);}
    public void setListener(@Nullable IButtonActionListener listener){this.listener = listener;}
    @SuppressWarnings("unused")
    public @Nullable IButtonActionListener getListener(){return listener;}
    @Override public void getButtonOptions(ArrayList<ButtonOption> res) {
        res.add(new ButtonOption(1, this, ()->buttonName, buttonGenericAllocator));
    }
    @Override public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
        if(listener != null) listener.actionPerformedWithButton(button, mouseButton);
    }
    @Override public @Nullable JsonElement getAsJsonElement() {return null;}
    @Override public UpdateTodo setValueFromJsonElementEx(@NotNull JsonElement data) {return new UpdateTodo();}
}
