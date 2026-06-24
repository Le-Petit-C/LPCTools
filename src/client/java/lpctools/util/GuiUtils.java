package lpctools.util;

import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.render.GuiContext;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lpctools.lpcfymasaapi.widgets.WHAutoAdjustStringWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;

public class GuiUtils {
    public static boolean isInTextOrGui(){
        Minecraft client = Minecraft.getInstance();
        return client.gui.screen() != null && /*client.getOverlay() == null &&*/ client.player != null;
    }
    
    public static void renderInfoWidgets(Object2LongOpenHashMap<WidgetBase> infoWidgets, GuiGraphicsExtractor drawContext, int mouseX, int mouseY){
        ArrayList<WidgetBase> removedWidgets = null;
        long currentTimeMillis = System.currentTimeMillis();
        for(var entry : infoWidgets.object2LongEntrySet()){
            var widget = entry.getKey();
            if(entry.getLongValue() < currentTimeMillis){
                if(removedWidgets == null) removedWidgets = new ArrayList<>();
                removedWidgets.add(widget);
            }
            else widget.render(GuiContext.fromGuiGraphics(drawContext), mouseX, mouseY, widget.isMouseOver(mouseX, mouseY));
        }
        if(removedWidgets != null) removedWidgets.forEach(infoWidgets::removeLong);
    }
    
    public static void cursorInfo(Object2LongOpenHashMap<WidgetBase> infoWidgets, String text, int sustainMillis, int screenWidth){
        var mouse = Minecraft.getInstance().mouseHandler;
        var window = Minecraft.getInstance().getWindow();
        WHAutoAdjustStringWidget widget = new WHAutoAdjustStringWidget(
            (int)mouse.getScaledXPos(window), (int)mouse.getScaledYPos(window),
            WHAutoAdjustStringWidget.Align.RIGHT_UP, text
        );
        if(widget.getX() + widget.getWidth() > screenWidth) widget.setAlign(widget.getAlign().XOpposite());
        if(widget.getY() < 0) widget.setAlign(widget.getAlign().YOpposite());
        infoWidgets.put(widget, System.currentTimeMillis() + sustainMillis);
    }
}
