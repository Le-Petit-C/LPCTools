package lpctools.util;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Contract;
import org.joml.*;

import java.lang.Math;

@SuppressWarnings("unused")
public class MathUtils {
    @Contract(pure = true)
    public static Matrix4f inverseOffsetMatrix4f(Vector3f offset){
        return new Matrix4f().setColumn(3, new Vector4f(offset.mul(-1), 1));
    }
    @Contract(pure = true)
    public static Matrix4f worldToCameraMatrix4f(Camera camera){
        Vector3f vec = camera.getPos().toVector3f().mul(-1);
        Matrix4f matrix =  new Matrix4f()
                .rotate(camera.getPitch() / 180 * MathHelper.PI, new Vector3f(1, 0, 0))
                .rotate((camera.getYaw() + 180) / 180 * MathHelper.PI, new Vector3f(0, 1, 0));
        return matrix.setColumn(3, matrix.transform(new Vector4f(vec, 1)));
    }
    @Contract(pure = true)
    public static Matrix4d worldToCameraMatrix4d(Camera camera){
        Vec3d vec3d = camera.getPos();
        Vector3d vec = new Vector3d(vec3d.getX(), vec3d.getY(), vec3d.getZ()).mul(-1);
        Matrix4d matrix =  new Matrix4d()
                .rotate(camera.getPitch() / 180 * Math.PI, new Vector3d(1, 0, 0))
                .rotate((camera.getYaw() + 180) / 180 * Math.PI, new Vector3d(0, 1, 0));
        return matrix.setColumn(3, matrix.transform(new Vector4d(vec, 1)));
    }
    @Contract(pure = true)
    public static int getManhattanDistanceToZero(Vec3i pos){
        return Math.abs(pos.getX()) + Math.abs(pos.getY()) + Math.abs(pos.getZ());
    }
    @Contract(pure = true)
    public static double squaredDistance(Vec3d pos, ChunkPos chunkPos){
        return square(chunkPos.x * 16 + 8.0 - pos.x) + square(chunkPos.z * 16 + 8.0 - pos.z);
    }
    @Contract(pure = true)
    public static Vector3i getSubChunkPos(BlockPos pos){
        return new Vector3i(pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
    }
    @Contract(pure = true)
    public static double square(double x){return x * x;}
}
