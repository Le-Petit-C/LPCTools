package lpctweaks.configbutton;

import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;

public class BooleanButton extends ButtonBase {
    public BooleanButton(int x, int y, int width, String text, IButtonActionListener actionListener){
        super(x, y, width, 20, text, actionListener);
    }
}
