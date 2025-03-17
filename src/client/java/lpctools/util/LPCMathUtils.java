package lpctools.util;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class LPCMathUtils {
    public static Vec3i floorVec3d(Vec3d p){
        return new Vec3i((int) Math.floor(p.getX()), (int) Math.floor(p.getY()), (int) Math.floor(p.getZ()));
    }
    public static Vec3d getBlockCenterPos(Vec3i blockPos){
        return new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
    }
}
