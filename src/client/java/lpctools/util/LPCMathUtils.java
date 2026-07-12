package lpctools.util;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class LPCMathUtils {
    public static Vec3i floorVec3d(Vec3 p){
        return new Vec3i((int) Math.floor(p.x()), (int) Math.floor(p.y()), (int) Math.floor(p.z()));
    }
}
