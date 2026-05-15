package lpctools.lpcfymasaapi.configButtons.uniqueConfigs;

import fi.dy.masa.malilib.config.IConfigResettable;
import lpctools.lpcfymasaapi.configButtons.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigReadable;
import lpctools.lpcfymasaapi.interfaces.ILPCUniqueConfigBase;
import lpctools.lpcfymasaapi.interfaces.ILPCValueChangeCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

@SuppressWarnings("unused")
public class BlockPosConfig extends ThirdListConfig implements IConfigResettable {
    private final @NotNull _INTConfig x, y, z;
    @Override public void getButtonOptions(ButtonOptionArrayList res) {
        super.getButtonOptions(res);
        if(expanded){
            res.add(new ButtonOption(1, (button, mouseButton)->{
                LocalPlayer player = Minecraft.getInstance().player;
                if(player != null) setPos(player.blockPosition());
            }, ()->Component.translatable("lpctools.configs.utils.blockPosConfig.setToPlayer").getString(), buttonGenericAllocator));
            res.add(new ButtonOption(1, (button, mouseButton)->{
                if(Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult)
                    setPos(hitResult.getBlockPos());
            }, ()->Component.translatable("lpctools.configs.utils.blockPosConfig.setToTarget").getString(), buttonGenericAllocator));
        }
        else {
            res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, x));
            res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, y));
            res.add(ILPCUniqueConfigBase.textFieldConfigValuePreset(1, z));
        }
    }
    
    public BlockPosConfig(ILPCConfigReadable parent, String nameKey, BlockPos defaultPos, @Nullable ILPCValueChangeCallback callback) {
        super(parent, nameKey, callback);
        x = addConfig(new _INTConfig(this, "x", defaultPos.getX()));
        y = addConfig(new _INTConfig(this, "y", defaultPos.getX()));
        z = addConfig(new _INTConfig(this, "z", defaultPos.getX()));
    }
    public Vector3i getPos(Vector3i res){
        return res.set(x.getAsInt(), y.getAsInt(), z.getAsInt());
    }
    public BlockPos.MutableBlockPos getPos(BlockPos.MutableBlockPos res){
        return res.set(x.getAsInt(), y.getAsInt(), z.getAsInt());
    }
    public BlockPos getPos(){
        return new BlockPos(x.getAsInt(), y.getAsInt(), z.getAsInt());
    }
    public Vector3i getDefaultPos(Vector3i res){
        return res.set(x.getDefaultIntegerValue(), y.getDefaultIntegerValue(), z.getDefaultIntegerValue());
    }
    public BlockPos.MutableBlockPos getDefaultPos(BlockPos.MutableBlockPos res){
        return res.set(x.getDefaultIntegerValue(), y.getDefaultIntegerValue(), z.getDefaultIntegerValue());
    }
    public BlockPos getDefaultPos(){
        return new BlockPos(x.getDefaultIntegerValue(), y.getDefaultIntegerValue(), z.getDefaultIntegerValue());
    }
    public void setPos(Vec3i pos){
        x.setIntegerValue(pos.getX());
        y.setIntegerValue(pos.getY());
        z.setIntegerValue(pos.getZ());
    }
    public void setPos(Vector3i pos){
        x.setIntegerValue(pos.x);
        y.setIntegerValue(pos.y);
        z.setIntegerValue(pos.z);
    }
    @Override public boolean isModified() {return x.isModified() || y.isModified() || z.isModified();}
    @Override public void resetToDefault() {setPos(getDefaultPos());}
    
    public static class _INTConfig extends IntegerConfig{
        public _INTConfig(BlockPosConfig parent, String nameKey, int defaultInteger) {
            super(parent, nameKey, defaultInteger, parent::onValueChanged);
            useSlider(false);
        }
        @Override public @NotNull String getNameTranslation() {
            return getName();
        }
        @Override public String getComment() {
            return getName() + ' ' + Component.translatable("lpctools.configs.utils.blockPosConfig.coordinate").getString();
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            getPage().markNeedUpdate();
        }
    }
}
