package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import fi.dy.masa.malilib.config.IConfigResettable;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.DoubleConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

@SuppressWarnings("unused")
public class Vector3dConfig extends ThirdListConfig implements IConfigResettable {
    private final @NotNull Vector3dConfig._DoubleConfig x, y, z;
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        super.getButtonOptions(res);
        if(expanded){
            res.add(new ButtonOption(1, (button, mouseButton)->{
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if(player != null) setPos(player.getPos());
            }, ()->Text.translatable("lpctools.configs.utils.blockPosConfig.setToPlayer").getString(), buttonGenericAllocator));
            res.add(new ButtonOption(1, (button, mouseButton)->{
                if(MinecraftClient.getInstance().crosshairTarget instanceof BlockHitResult hitResult)
                    setPos(hitResult.getPos());
            }, ()->Text.translatable("lpctools.configs.utils.blockPosConfig.setToTarget").getString(), buttonGenericAllocator));
        }
        else {
            res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, x));
            res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, y));
            res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, z));
        }
    }
    
    public Vector3dConfig(ILPCConfigReadable parent, String nameKey, Vec3d defaultPos, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        x = addConfig(new _DoubleConfig(this, "x", defaultPos.getX()));
        y = addConfig(new _DoubleConfig(this, "y", defaultPos.getX()));
        z = addConfig(new _DoubleConfig(this, "z", defaultPos.getX()));
    }
    public Vector3d getPos(Vector3d res){
        return res.set(x.getAsDouble(), y.getAsDouble(), z.getAsDouble());
    }
    public Vec3d getPos(){
        return new Vec3d(x.getAsDouble(), y.getAsDouble(), z.getAsDouble());
    }
    public Vector3d getDefaultPos(Vector3d res){
        return res.set(x.getDefaultDoubleValue(), y.getDefaultDoubleValue(), z.getDefaultDoubleValue());
    }
    public Vec3d getDefaultPos(){
        return new Vec3d(x.getDefaultDoubleValue(), y.getDefaultDoubleValue(), z.getDefaultDoubleValue());
    }
    public void setPos(Vec3d pos){
        x.setDoubleValue(pos.getX());
        y.setDoubleValue(pos.getY());
        z.setDoubleValue(pos.getZ());
    }
    public void setPos(Vector3d pos){
        x.setDoubleValue(pos.x);
        y.setDoubleValue(pos.y);
        z.setDoubleValue(pos.z);
    }
    @Override public boolean isModified() {return x.isModified() || y.isModified() || z.isModified();}
    @Override public void resetToDefault() {setPos(getDefaultPos());}
    
    public static class _DoubleConfig extends DoubleConfig {
        public _DoubleConfig(Vector3dConfig parent, String nameKey, double defaultDouble) {
            super(parent, nameKey, defaultDouble, -Double.MAX_VALUE / 2, Double.MAX_VALUE / 2, parent::onValueChanged);
        }
        @Override public @NotNull String getNameTranslation() {
            return getName();
        }
        @Override public String getComment() {
            return getName() + ' ' + Text.translatable("lpctools.configs.utils.blockPosConfig.coordinate").getString();
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            getPage().markNeedUpdate();
        }
    }
}
