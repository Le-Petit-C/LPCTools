package lpctools.lpcfymasaapi.render;

import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Matrix4f;

public class RenderEventHandler {
	static final String preMainProfilerString = LPCTools.modReference.modId + "_pre_main";

	public static void runRenderWorldPreMain(Matrix4f posMatrix, Matrix4f projMatrix,
											 @SuppressWarnings("unused") Minecraft mc,
											 Frustum frustum, Camera camera,
											 ProfilerFiller profiler) {
		profiler.push(preMainProfilerString);
		if (!Registries.PRE_MAIN.isEmpty())
			Registries.PRE_MAIN.runner().onRenderWorldPreMain(new Registries.MASAWorldRenderContext(posMatrix, projMatrix, frustum, camera, profiler));
		profiler.pop();
	}
}
