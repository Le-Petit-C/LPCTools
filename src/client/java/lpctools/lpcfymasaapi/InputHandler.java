package lpctools.lpcfymasaapi;


import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.*;
import lpctools.lpcfymasaapi.configbutton.ILPCHotkey;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InputHandler implements IKeybindProvider {
    @NotNull final Reference modReference;
    private ArrayList<ILPCHotkey> keysToAdd;
    public InputHandler(@NotNull Reference modReference) {
        super();
        this.modReference = modReference;
        InputEventHandler.getKeybindManager().registerKeybindProvider(this);
    }
    public void addHotkey(ILPCHotkey key){
        if(keysToAdd == null)
            keysToAdd = new ArrayList<>();
        keysToAdd.add(key);
        InputEventHandler.getKeybindManager().addKeybindToMap(key.LPCGetKeybind());
    }
    @Override public void addKeysToMap(IKeybindManager manager) {
        if(keysToAdd == null) return;
        for (ILPCHotkey hotkey : keysToAdd)
            manager.addKeybindToMap(hotkey.LPCGetKeybind());
    }
    @Override public void addHotkeys(IKeybindManager manager) {
        if(keysToAdd == null) return;
        IHotkey[] array = new IHotkey[keysToAdd.size()];
        for(int a = 0; a < array.length; ++a) array[a] = keysToAdd.get(a).LPCGetHotkey();
        manager.addHotkeysForCategory(modReference.modName, "lpctools.hotkeys", List.of(array));
    }
}
