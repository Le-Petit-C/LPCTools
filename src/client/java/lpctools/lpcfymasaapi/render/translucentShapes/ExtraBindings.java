package lpctools.lpcfymasaapi.render.translucentShapes;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;

public interface ExtraBindings {
	@SuppressWarnings("unused")
	ExtraBindings LIGHT_MAP = pass->pass.bindTexture("Sampler2",
		Minecraft.getInstance().gameRenderer.levelLightmap(),
		RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
	);
	
	void bindExtra(RenderPass renderPass);
}
