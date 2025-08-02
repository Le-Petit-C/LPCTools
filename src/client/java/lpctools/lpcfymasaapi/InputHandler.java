package lpctools.lpcfymasaapi;


import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;

public class InputHandler implements IKeybindProvider {
    @NotNull final Reference modReference;
    private HashSet<IHotkey> keysToAdd;
    public InputHandler(@NotNull Reference modReference) {
        super();
        this.modReference = modReference;
        InputEventHandler.getKeybindManager().registerKeybindProvider(this);
    }
    public void addHotkey(IHotkey key){
        if(keysToAdd == null)
            keysToAdd = new HashSet<>();
        keysToAdd.add(key);
        InputEventHandler.getKeybindManager().addKeybindToMap(key.getKeybind());
    }
    public void removeHotkey(IHotkey key){
        if(keysToAdd != null)
            keysToAdd.remove(key);
    }
    @Override public void addKeysToMap(IKeybindManager manager) {
        if(keysToAdd == null) return;
        for (IHotkey hotkey : keysToAdd)
            manager.addKeybindToMap(hotkey.getKeybind());
    }
    @Override public void addHotkeys(IKeybindManager manager) {
        if(keysToAdd == null) return;
        IHotkey[] array = new IHotkey[keysToAdd.size()];
        int a = 0;
        for(IHotkey iHotkey : keysToAdd)
            array[a++] = iHotkey;
        manager.addHotkeysForCategory(modReference.modName, "lpctools.hotkeys", List.of(array));
    }
}
