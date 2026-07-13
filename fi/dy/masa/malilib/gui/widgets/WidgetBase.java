package fi.dy.masa.malilib.gui.widgets;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_4588;

public abstract class WidgetBase {
    protected final class_310 mc;
    protected final class_327 textRenderer;
    protected final int fontHeight;
    protected class_332 drawContext;
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected int zLevel;

    public WidgetBase(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.mc = class_310.method_1551();
        this.textRenderer = this.mc.field_1772;
        this.fontHeight = 9;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setZLevel(int zLevel) {
        this.zLevel = zLevel;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;
    }

    public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
        return this.isMouseOver(mouseX, mouseY) ? this.onMouseClickedImpl(mouseX, mouseY, mouseButton) : false;
    }

    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {
        return false;
    }

    public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
        this.onMouseReleasedImpl(mouseX, mouseY, mouseButton);
    }

    public void onMouseReleasedImpl(int mouseX, int mouseY, int mouseButton) {
    }

    public boolean onMouseScrolled(int mouseX, int mouseY, double horizontalAmount, double verticalAmount) {
        return this.isMouseOver(mouseX, mouseY) ? this.onMouseScrolledImpl(mouseX, mouseY, horizontalAmount, verticalAmount) : false;
    }

    public boolean onMouseScrolledImpl(int mouseX, int mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    public boolean onKeyTyped(int keyCode, int scanCode, int modifiers) {
        return this.onKeyTypedImpl(keyCode, scanCode, modifiers);
    }

    protected boolean onKeyTypedImpl(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    public boolean onCharTyped(char charIn, int modifiers) {
        return this.onCharTypedImpl(charIn, modifiers);
    }

    protected boolean onCharTypedImpl(char charIn, int modifiers) {
        return false;
    }

    public boolean canSelectAt(int mouseX, int mouseY, int mouseButton) {
        return this.isMouseOver(mouseX, mouseY);
    }

    public class_4588 bindTexture(class_2960 texture, class_332 context) {
        return RenderUtils.bindGuiTexture(texture, context);
    }

    public class_4588 bindOverlayTexture(class_2960 texture, class_332 context) {
        return RenderUtils.bindGuiOverlayTexture(texture, context);
    }

    public int getStringWidth(String text) {
        return this.textRenderer.method_1727(text);
    }

    public void drawString(int x, int y, int color, String text, class_332 drawContext) {
        drawContext.method_51433(this.textRenderer, text, x, y, color, false);
        RenderUtils.forceDraw(drawContext);
    }

    public void drawCenteredString(int x, int y, int color, String text, class_332 drawContext) {
        drawContext.method_51433(this.textRenderer, text, x - this.getStringWidth(text) / 2, y, color, false);
        RenderUtils.forceDraw(drawContext);
    }

    public void drawStringWithShadow(int x, int y, int color, String text, class_332 drawContext) {
        drawContext.method_25303(this.textRenderer, text, x, y, color);
        RenderUtils.forceDraw(drawContext);
    }

    public void drawCenteredStringWithShadow(int x, int y, int color, String text, class_332 drawContext) {
        drawContext.method_25300(this.textRenderer, text, x, y, color);
        RenderUtils.forceDraw(drawContext);
    }

    public void drawBackgroundMask(class_332 drawContext) {
        RenderUtils.drawTexturedRectAndDraw(GuiBase.BG_TEXTURE, this.x + 1, this.y + 1, 0, 0, this.width - 2, this.height - 2, drawContext);
    }

    public void render(int mouseX, int mouseY, boolean selected, class_332 drawContext) {
        if (this.drawContext == null || !this.drawContext.equals(drawContext)) {
            this.drawContext = drawContext;
        }
    }

    public void postRenderHovered(int mouseX, int mouseY, boolean selected, class_332 drawContext) {
        if (this.drawContext == null || !this.drawContext.equals(drawContext)) {
            this.drawContext = drawContext;
        }
    }
}
