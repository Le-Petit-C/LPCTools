package lpctools.lpcfymasaapi.configbutton.derivedConfigs;

import lpctools.lpcfymasaapi.configbutton.transferredConfigs.IntegerConfig;
import lpctools.lpcfymasaapi.interfaces.ILPCConfigList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

@SuppressWarnings("unused")
public class BlockPosConfig extends ThirdListConfig{
    public @NotNull _INTConfig x, y, z;
    public BlockPosConfig(ILPCConfigList parent, String nameKey, BlockPos defaultPos, boolean defaultBoolean) {
        super(parent, nameKey, defaultBoolean);
        x = addConfig(new _INTConfig(this, "x", defaultPos.getX()));
        y = addConfig(new _INTConfig(this, "y", defaultPos.getX()));
        z = addConfig(new _INTConfig(this, "z", defaultPos.getX()));
    }
    public Vector3i getPos(Vector3i res){
        return res.set(x.getAsInt(), y.getAsInt(), z.getAsInt());
    }
    public BlockPos.Mutable getPos(BlockPos.Mutable res){
        return res.set(x.getAsInt(), y.getAsInt(), z.getAsInt());
    }
    public BlockPos getPos(){
        return new BlockPos(x.getAsInt(), y.getAsInt(), z.getAsInt());
    }
    public Vector3i getDefaultPos(Vector3i res){
        return res.set(x.getDefaultIntegerValue(), y.getDefaultIntegerValue(), z.getDefaultIntegerValue());
    }
    public BlockPos.Mutable getDefaultPos(BlockPos.Mutable res){
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
    @Override public void onValueChanged() {
        super.onValueChanged();
        getPage().updateIfCurrent();
    }
    
    public static class _INTConfig extends IntegerConfig{
        public _INTConfig(ILPCConfigList parent, String nameKey, int defaultInteger) {
            super(parent, nameKey, defaultInteger);
            useSlider(false);
        }
        @Override public @NotNull String getNameTranslation() {
            return getName();
        }
        @Override public String getComment() {
            return getName() + ' ' + Text.translatable("lpctools.configs.utils.coordinateComment").getString();
        }
        @Override public void onValueChanged() {
            super.onValueChanged();
            getPage().updateIfCurrent();
        }
    }
}
