package lpctools.lpcfymasaapi;


import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.hotkeys.*;
import lpctools.util.TaskUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

import static lpctools.util.TaskUtils.*;

public class InputHandler implements IKeybindProvider {
    @NotNull final Reference modReference;
    private HashSet<IKeybind> keysToAdd;
    public InputHandler(@NotNull Reference modReference) {
        super();
        this.modReference = modReference;
        InputEventHandler.getKeybindManager().registerKeybindProvider(this);
    }
    public void addKeybind(IKeybind keybind){
        if(keysToAdd == null)
            keysToAdd = new HashSet<>();
        keysToAdd.add(keybind);
        //InputEventHandler.getKeybindManager().addKeybindToMap(keybind);
        markNeedUpdateKeys();
    }
    public void addHotkey(IHotkey key){
        addKeybind(key.getKeybind());
    }
    public void removeKeybind(IKeybind keybind){
        if(keysToAdd != null)
            keysToAdd.remove(keybind);
        markNeedUpdateKeys();
    }
    public void removeHotkey(IHotkey key){
        removeKeybind(key.getKeybind());
    }
    @Override public void addKeysToMap(IKeybindManager manager) {
        if(keysToAdd == null) return;
        for (IKeybind keybind : keysToAdd)
            manager.addKeybindToMap(keybind);
    }
    @Override public void addHotkeys(IKeybindManager manager) {
        //TODO:测试keybind是否还正常
        if(keysToAdd == null) return;
        //IKeybind[] array = new IKeybind[keysToAdd.size()];
        //int a = 0;
        for(IKeybind keybind : keysToAdd)
            manager.addKeybindToMap(keybind);
            //array[a++] = keybind;
        //manager.addHotkeysForCategory(modReference.modName, "lpctools.hotkeys", List.of(array));
    }
    private static void markNeedUpdateKeys(){markRegisteredTask(taskKey);}
    private static final TaskUtils.TaskKey taskKey = registerTask(()->InputEventHandler.getKeybindManager().updateUsedKeys());
}
