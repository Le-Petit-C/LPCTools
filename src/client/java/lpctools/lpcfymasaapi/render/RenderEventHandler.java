package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import lpctools.LPCTools;
import lpctools.lpcfymasaapi.Registries;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

public class RenderEventHandler {
	static final String preMainProfilerString = LPCTools.modReference.modId + "_pre_main";
	
	/** 以{@link fi.dy.masa.malilib.event.RenderEventHandler#runRenderWorldPreWeather}为基础进行的修改 */
	@SuppressWarnings("UnstableApiUsage") @ApiStatus.Internal
	public static void runRenderWorldPreMain(Matrix4f posMatrix, Matrix4f projMatrix, @SuppressWarnings("unused") Minecraft mc,
											 FrameGraphBuilder frameGraphBuilder, LevelTargetBundle fbSet,
											 Frustum frustum, Camera camera, RenderBuffers buffers,
											 ProfilerFiller profiler) {
		profiler.push(preMainProfilerString);
		if (!Registries.PRE_MAIN.isEmpty()) {
			FramePass pass = frameGraphBuilder.addPass(preMainProfilerString);
			fbSet.main = pass.readsAndWrites(fbSet.main);
			ResourceHandle<RenderTarget> handleMain = fbSet.main;
			pass.executes(() -> {
				GpuBufferSlice fog = RenderSystem.getShaderFog();
				RenderTarget fb = handleMain.get();
				Registries.PRE_MAIN.runner().onRenderWorldPreMain(new Registries.MASAWorldRenderContext(fb, posMatrix, projMatrix, frustum, camera, buffers, profiler));
				RenderSystem.setShaderFog(fog);
			});
			if (!Registries.PRE_MAIN.isEmpty())
				pass.disableCulling();
		}
		profiler.pop();
	}
}
