package lpctools.lpcfymasaapi.render.translucentShapes;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.MinecraftClient;

public interface ExtraBindings {
	@SuppressWarnings("unused")
	ExtraBindings LIGHT_MAP = pass->pass.bindSampler("Sampler2",
		MinecraftClient.getInstance().gameRenderer.getLightmapTextureManager().getGlTextureView()
	);
	
	void bindExtra(RenderPass renderPass);
}
