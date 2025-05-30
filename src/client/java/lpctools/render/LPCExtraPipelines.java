package lpctools.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

public class LPCExtraPipelines {
    static {
        RenderPipelines.getAll();
    }
    public static RenderPipeline POSITION_COLOR_MASA_NO_CULL =
        RenderPipeline.builder(MaLiLibPipelines.POSITION_COLOR_MASA_STAGE)
            .withLocation(Identifier.of(MaLiLibReference.MOD_ID, "pipeline/position_color/masa/no_cull"))
            .withCull(false)
            .withDepthWrite(true)
            .build();
}
