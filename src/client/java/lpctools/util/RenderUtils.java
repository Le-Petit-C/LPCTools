package lpctools.util;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;

import java.util.Objects;

public class RenderUtils {
	public static GpuTextureView colorAttachmentViewOrDef(RenderTarget target) {
		return Objects.requireNonNullElse(target.getColorTextureView(), Minecraft.getInstance().getMainRenderTarget().getColorTextureView());
	}
}
