package lpctools.lpcfymasaapi.configbutton;

import fi.dy.masa.malilib.hotkeys.IHotkey;
import fi.dy.masa.malilib.hotkeys.IKeybind;

public interface ILPCHotkey {
    IHotkey LPCGetHotkey();
    default IKeybind LPCGetKeybind(){
        return LPCGetHotkey().getKeybind();
    }
}
