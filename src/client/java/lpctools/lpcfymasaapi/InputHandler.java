package lpctools.lpcfymasaapi;


import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.*;
import fi.dy.masa.malilib.util.GuiUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InputHandler implements IKeybindProvider, IKeyboardInputHandler, IMouseInputHandler
{
    @NotNull Reference modReference;

    private ArrayList<IHotkey> keysToAdd;
    public InputHandler(@NotNull Reference modReference) {
        super();
        this.modReference = modReference;
        InputEventHandler.getKeybindManager().registerKeybindProvider(this);
    }

    public void addHotkey(IHotkey key){
        if(keysToAdd == null)
            keysToAdd = new ArrayList<>();
        keysToAdd.add(key);
        InputEventHandler.getKeybindManager().addKeybindToMap(key.getKeybind());
    }

    @Override
    public void addKeysToMap(IKeybindManager manager)
    {
        if(keysToAdd == null) return;
        for (IHotkey hotkey : keysToAdd) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }

    @Override
    public void addHotkeys(IKeybindManager manager)
    {
        if(keysToAdd == null) return;
        manager.addHotkeysForCategory(modReference.modName, "lpctools.hotkeys", keysToAdd);
    }

    @Override
    public boolean onKeyInput(int keyCode, int scanCode, int modifiers, boolean eventKeyState)
    {
        // Not in a GUI
        /*if (GuiUtils.getCurrentScreen() == null && eventKeyState)
        {
        }*/
        return false;
    }

    @Override
    public boolean onMouseClick(int mouseX, int mouseY, int eventButton, boolean eventButtonState)
    {
        // Not in a GUI
        if (GuiUtils.getCurrentScreen() == null) {
            //TODO:左键单击关闭edge_filler
        }
        return false;
    }

    @Override
    public boolean onMouseScroll(int mouseX, int mouseY, double dWheel)
    {
        // Not in a GUI
        /*if (GuiUtils.getCurrentScreen() == null && dWheel != 0)
        {
        }*/

        return false;
    }

}
