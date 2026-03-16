package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

public class RenderEventHandler {
	static final String preMainProfilerString = LPCTools.modReference.modId + "_pre_main";
	
	/** 以{@link fi.dy.masa.malilib.event.RenderEventHandler#runRenderWorldPreWeather}为基础进行的修改 */
	@SuppressWarnings("UnstableApiUsage") @ApiStatus.Internal
	public static void runRenderWorldPreMain(Matrix4f posMatrix, Matrix4f projMatrix, @SuppressWarnings("unused") MinecraftClient mc,
											 FrameGraphBuilder frameGraphBuilder, DefaultFramebufferSet fbSet,
											 Frustum frustum, Camera camera,
											 Profiler profiler) {
		profiler.push(preMainProfilerString);
		if (!Registries.PRE_MAIN.isEmpty()) {
			RenderPass pass = frameGraphBuilder.createPass(preMainProfilerString);
			fbSet.mainFramebuffer = pass.transfer(fbSet.mainFramebuffer);
			pass.setRenderer(() -> {
				Fog fog = RenderSystem.getShaderFog();
				Registries.PRE_MAIN.runner().onRenderWorldPreMain(new Registries.MASAWorldRenderContext(posMatrix, projMatrix, frustum, camera, profiler));
				RenderSystem.setShaderFog(fog);
			});
			if (!Registries.PRE_MAIN.isEmpty())
				pass.markToBeVisited();
		}
		profiler.pop();
	}
}
