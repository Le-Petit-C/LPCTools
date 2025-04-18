package lpctools.util;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@SuppressWarnings("unused")
public class MathUtils {
    public static Matrix4f inverseOffsetMatrix4f(Vector3f offset){
        return new Matrix4f().setColumn(3, new Vector4f(offset.mul(-1), 1));
    }
    public static Matrix4f worldToCameraMatrix(Camera camera){
        Vector3f vec = camera.getPos().toVector3f().mul(-1);
        Matrix4f matrix =  new Matrix4f()
                .rotate(camera.getPitch() / 180 * MathHelper.PI, new Vector3f(1, 0, 0))
                .rotate((camera.getYaw() + 180) / 180 * MathHelper.PI, new Vector3f(0, 1, 0));
        return matrix.setColumn(3, matrix.transform(new Vector4f(vec, 1)));
    }
}
