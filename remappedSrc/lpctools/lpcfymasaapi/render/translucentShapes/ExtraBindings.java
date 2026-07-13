package lpctools.lpcfymasaapi.render.translucentShapes;

import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.Minecraft;

public interface ExtraBindings {
	@SuppressWarnings("unused")
	ExtraBindings LIGHT_MAP = pass->pass.bindSampler("Sampler2",
		Minecraft.getInstance().gameRenderer.lightTexture().getTarget()
	);
	
	void bindExtra(RenderPass renderPass);
}
