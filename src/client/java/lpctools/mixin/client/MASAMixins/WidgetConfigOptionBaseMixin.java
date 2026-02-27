package lpctools.mixin.client.MASAMixins;

import fi.dy.masa.malilib.config.gui.ConfigOptionChangeListenerTextField;
import fi.dy.masa.malilib.gui.GuiTextFieldGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetConfigOptionBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fi.dy.masa.malilib.gui.wrappers.TextFieldWrapper;
import lpctools.mixinInterfaces.MASAMixins.IWidgetConfigOptionBaseEx;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
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
    void drawExtraTextFields(DrawContext context, int mouseX, int mouseY, CallbackInfo ci){
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers)
            textFieldWrapper.getTextField().render(context, mouseX, mouseY, 0f);
    }
    @Inject(method = "onMouseClickedImpl", at = @At("RETURN"), cancellable = true)
    void onMouseClickedImpl(Click click, boolean doubleClick, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) return;
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers){
            if(textFieldWrapper.mouseClicked(click, doubleClick)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
    @Inject(method = "onKeyTypedImpl", at = @At("RETURN"), cancellable = true)
    void onKeyTypedImpl(KeyInput input, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) return;
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers){
            if(textFieldWrapper.onKeyTyped(input)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
    @Inject(method = "onCharTypedImpl", at = @At("RETURN"), cancellable = true)
    void onCharTypedImpl(CharInput input, CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue()) return;
        for(TextFieldWrapper<? extends GuiTextFieldGeneric> textFieldWrapper : extraTextFieldWrappers){
            if(textFieldWrapper.onCharTyped(input)) {
                cir.setReturnValue(true);
                return;
            }
        }
    }
}
