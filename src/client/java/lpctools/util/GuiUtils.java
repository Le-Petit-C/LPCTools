package lpctools.util;

import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lpctools.lpcfymasaapi.widgets.WHAutoAdjustStringWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;

public class GuiUtils {
    public static boolean isInTextOrGui(){
        MinecraftClient client = MinecraftClient.getInstance();
        return client.currentScreen != null && /*client.getOverlay() == null &&*/ client.player != null;
    }
    
    public static void renderInfoWidgets(Object2LongOpenHashMap<WidgetBase> infoWidgets, DrawContext drawContext, int mouseX, int mouseY){
        ArrayList<WidgetBase> removedWidgets = null;
        long currentTimeMillis = System.currentTimeMillis();
        for(var entry : infoWidgets.object2LongEntrySet()){
            var widget = entry.getKey();
            if(entry.getLongValue() < currentTimeMillis){
                if(removedWidgets == null) removedWidgets = new ArrayList<>();
                removedWidgets.add(widget);
            }
            else widget.render(drawContext, mouseX, mouseY, widget.isMouseOver(mouseX, mouseY));
        }
        if(removedWidgets != null) removedWidgets.forEach(infoWidgets::removeLong);
    }
    
    public static void cursorInfo(Object2LongOpenHashMap<WidgetBase> infoWidgets, String text, int sustainMillis, int screenWidth){
        var mouse = MinecraftClient.getInstance().mouse;
        var window = MinecraftClient.getInstance().getWindow();
        WHAutoAdjustStringWidget widget = new WHAutoAdjustStringWidget(
            (int)mouse.getScaledX(window), (int)mouse.getScaledY(window),
            WHAutoAdjustStringWidget.Align.RIGHT_UP, text
        );
        if(widget.getX() + widget.getWidth() > screenWidth) widget.setAlign(widget.getAlign().XOpposite());
        if(widget.getY() < 0) widget.setAlign(widget.getAlign().YOpposite());
        infoWidgets.put(widget, System.currentTimeMillis() + sustainMillis);
    }
}
