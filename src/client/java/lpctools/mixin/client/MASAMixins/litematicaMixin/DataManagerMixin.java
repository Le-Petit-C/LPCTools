package lpctools.mixin.client.MASAMixins.litematicaMixin;

import com.google.gson.JsonObject;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.malilib.interfaces.IRangeChangeListener;
import fi.dy.masa.malilib.util.LayerRange;
import lpctools.lpcfymasaapi.Registries;
import lpctools.mixin.client.MASAMixins.LayerRangeMixin;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lpctools.mixinInterfaces.MASAMixins.MuteLayerRangeRefresherMixin.setRefresher;

@Pseudo @Mixin(value = DataManager.class, remap = false)
public class DataManagerMixin{
    @Shadow private LayerRange renderRange;
    @Inject(method = "fromJson", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER,
        target = "Lfi/dy/masa/litematica/data/DataManager;renderRange:Lfi/dy/masa/malilib/util/LayerRange;"))
    void onRenderRangeModified(JsonObject obj, CallbackInfo ci){
        IRangeChangeListener refresher = ((LayerRangeMixin)renderRange).getRefresher();
        IRangeChangeListener myRefresher = Registries.LITEMATICA_RANGE_CHANGED.run();
        if(refresher == null) setRefresher(renderRange, myRefresher);
        else setRefresher(renderRange, new IRangeChangeListener() {
            @Override public void updateAll() {
                refresher.updateAll();
                myRefresher.updateAll();
            }
            @Override public void updateBetweenX(int minX, int maxX) {
                refresher.updateBetweenX(minX, maxX);
                myRefresher.updateBetweenX(minX, maxX);
            }
            @Override public void updateBetweenY(int minY, int maxY) {
                refresher.updateBetweenY(minY, maxY);
                myRefresher.updateBetweenY(minY, maxY);
            }
            @Override public void updateBetweenZ(int minZ, int maxZ) {
                refresher.updateBetweenZ(minZ, maxZ);
                myRefresher.updateBetweenZ(minZ, maxZ);
            }
        });
        myRefresher.updateAll();
    }
}
