package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import lpctools.mixinInterfaces.MASAMixins.IWidgetConfigOptionBaseEx;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = WidgetConfigOptionBase.class, remap = false)
public class WidgetConfigOptionBaseMixin implements IWidgetConfigOptionBaseEx {
    @Shadow @Final protected WidgetListConfigOptionsBase<?, ?> parent;
    @Unique ArrayList<TextFieldWrapper<? extends GuiTextFieldGeneric>> extraTextFieldWrappers = new ArrayList<>();
    @Unique @Override public void lPCTools$addExtraTextField(GuiTextFieldGeneric field, ConfigOptionChangeListenerTextField listener) {
        TextFieldWrapper<? extends GuiTextFieldGeneric> wrapper = new TextFieldWrapper<>(field, listener);
        extraTextFieldWrappers.add(wrapper);
        parent.addTextField(wrapper);
    }
    @Inject(method = "drawTextFields", at = @At("TAIL"))
    void drawExtraTextFields(int mouseX, int mouseY, DrawContext drawContext, CallbackInfo ci){
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers)
            textFieldWrapper.getTextField().render(drawContext, mouseX, mouseY, 0f);
    }
    @Inject(method = "onMouseClickedImpl", at = @At("RETURN"), cancellable = true)
    void onMouseClickedImpl(int mouseX, int mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) return;
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers){
            if(textFieldWrapper.mouseClicked(mouseX, mouseY, mouseButton)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
    @Inject(method = "onKeyTypedImpl", at = @At("RETURN"), cancellable = true)
    void onKeyTypedImpl(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) return;
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers){
            if(textFieldWrapper.onKeyTyped(keyCode, scanCode, modifiers)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
    @Inject(method = "onCharTypedImpl", at = @At("RETURN"), cancellable = true)
    void onCharTypedImpl(char charIn, int modifiers, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) return;
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers){
            if(textFieldWrapper.onCharTyped(charIn, modifiers)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
