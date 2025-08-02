package lpctools.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractFurnaceScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import static lpctools.tools.furnaceMaintainer.FurnaceMaintainer.*;

@Mixin(AbstractFurnaceScreen.class)
public class FurnaceMaintainer {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    void mixinScreenRender(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci){
        if(!FMConfig.getBooleanValue()) return;
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientPlayerInteractionManager itm = client.interactionManager;
        if(player == null || itm == null) {
            FMConfig.setBooleanValue(false);
            return;
        }
        ci.cancel();
        itm.clickSlot(player.currentScreenHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, player);
        MinecraftClient.getInstance().setScreen(null);
    }
}
