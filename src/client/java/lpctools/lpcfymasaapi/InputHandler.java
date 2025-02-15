package lpctools.lpcfymasaapi;


import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InputHandler implements IKeybindProvider {
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
    @Override public void addKeysToMap(IKeybindManager manager) {
        if(keysToAdd == null) return;
        for (IHotkey hotkey : keysToAdd) {
            manager.addKeybindToMap(hotkey.getKeybind());
        }
    }
    @Override public void addHotkeys(IKeybindManager manager) {
        if(keysToAdd == null) return;
        manager.addHotkeysForCategory(modReference.modName, "lpctools.hotkeys", keysToAdd);
    }
}
