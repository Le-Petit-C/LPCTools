package lpctools.util;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MathUtils {
    public static Matrix4f inverseOffsetMatrix4f(Vector3f offset){
        return new Matrix4f().setColumn(3, new Vector4f(offset.mul(-1), 1));
    }
}
