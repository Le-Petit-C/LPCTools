package lpctools.lpcfymasaapi.configbutton.uniqueConfigs;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ButtonConfig extends LPCConfigBase implements IButtonActionListener {
    @Nullable public IButtonActionListener listener;
    @Nullable public String buttonName;
    public ButtonConfig(ILPCConfigList parent, String nameKey, @Nullable String buttonName, @Nullable IButtonActionListener listener) {
        super(parent, nameKey, null);
        this.listener = listener;
        this.buttonName = buttonName;
    }
    public ButtonConfig(ILPCConfigList parent, String nameKey, @Nullable String buttonName) {
        super(parent, nameKey, null);
        this.buttonName = buttonName;
    }
    public ButtonConfig(ILPCConfigList parent, String nameKey, @Nullable IButtonActionListener listener) {
        super(parent, nameKey, null);
        this.listener = listener;
    }
    public ButtonConfig(ILPCConfigList parent, String nameKey) {
        super(parent, nameKey, null);
    }
    @Override protected List<ButtonOption> getButtonOptions() {
        return List.of(new ButtonOption(1, this, ()->buttonName));
    }
    @Override public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
        if(listener != null) listener.actionPerformedWithButton(button, mouseButton);
    }
}
