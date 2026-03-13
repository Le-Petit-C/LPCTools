package lpctools.debugs;

import lpctools.lpcfymasaapi.configButtons.uniqueConfigs.UniqueBooleanConfig;
import lpctools.lpcfymasaapi.render.translucentShapes.ShapeReference;
import lpctools.lpcfymasaapi.render.translucentShapes.Sphere;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class DrawSphere {
	public static UniqueBooleanConfig drawSphere = new UniqueBooleanConfig(DebugConfigs.debugs,
		"drawSphere", false, DrawSphere::drawSphereCallback);
	private static @Nullable ShapeReference reference;
	private static void drawSphereCallback(){
		if(drawSphere.getBooleanValue()) {
			if(reference == null)
				reference = Sphere.register(false).register(new Sphere(new Vector3d(), 0x7f7f7f7f, 1));
		}
		else {
			if(reference != null){
				reference.close();
				reference = null;
			}
		}
	}
}
