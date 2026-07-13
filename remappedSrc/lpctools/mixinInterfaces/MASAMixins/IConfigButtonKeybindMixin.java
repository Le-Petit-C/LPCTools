package lpctools.mixinInterfaces.MASAMixins;

import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import org.jetbrains.annotations.Nullable;

public interface IConfigButtonKeybindMixin {
	void setHost(@Nullable IKeybindConfigGui host);
}
