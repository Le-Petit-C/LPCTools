package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigResettable;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.gui.interfaces.IKeybindConfigGui;
import fi.dy.masa.malilib.gui.widgets.WidgetBase;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOption;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import lpctools.lpcfymasaapi.interfaces.ButtonBaseProvider;
import lpctools.lpcfymasaapi.interfaces.ButtonConsumer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WidgetConfigOption.class, remap = false)
public abstract class WidgetConfigOptionMixin extends WidgetConfigOptionBase<GuiConfigsBase.ConfigOptionWrapper> {
    @Shadow @Final protected IKeybindConfigGui host;
    public WidgetConfigOptionMixin(int x, int y, int width, int height, WidgetListConfigOptionsBase<?, ?> parent, GuiConfigsBase.ConfigOptionWrapper entry, int listIndex) {
        super(x, y, width, height, parent, entry, listIndex);
    }
    @Unique WidgetConfigOption getThis(){return (WidgetConfigOption)(Object)this;}
    @Inject(method = "addConfigOption", at = @At(value = "TAIL"), remap = false, cancellable = true)
    void test(int x, int y, float zLevel, int labelWidth, int configWidth, IConfigBase config, CallbackInfo ci){
        if(config instanceof ButtonBaseProvider buttonConsumer){
            buttonConsumer.addButtons(x, y, zLevel, labelWidth, configWidth, new ButtonConsumer() {
                @Override public <T extends ButtonBase> T addButton(T button, IButtonActionListener listener) {
                    return WidgetConfigOptionMixin.this.addButton(button, listener);
                }
                @Override public ButtonGeneric createResetButton(int x, int y, IConfigResettable config) {
                    return WidgetConfigOptionMixin.this.createResetButton(x, y, config);
                }
                @Override public IKeybindConfigGui getKeybindHost() {
                    return host;
                }
                @Override public <T extends WidgetBase> T addWidget(T widget) {
                    return WidgetConfigOptionMixin.this.addWidget(widget);
                }
                @Override public WidgetListConfigOptionsBase<?, ?> getWidgetListConfigOptionsBase() {
                    return parent;
                }
            });
            ci.cancel();
        }
    }
}
