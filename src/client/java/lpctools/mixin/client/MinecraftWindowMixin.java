package lpctools.mixin.client;

import lpctools.util.GlVersionMixinHandler;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Window.class)
public class MinecraftWindowMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", remap = false,
            target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
    private void resetRequiredGlVersion(CallbackInfo ci) {
        GlVersionMixinHandler.resetRequiredGlVersion();
    }
    @Mixin(value = GLFW.class, remap = false)
    private static class GLFWVersionRecord{
        @Inject(method = "glfwWindowHint", at = @At("HEAD"))
        private static void recordVersion(int hint, int value, CallbackInfo ci){
            GlVersionMixinHandler.recordVersion(hint, value);
        }
    }
}
