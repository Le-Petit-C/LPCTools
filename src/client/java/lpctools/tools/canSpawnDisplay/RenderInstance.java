package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.furtherWarpped.RenderBuffer;
import lpctools.shader.ShaderPrograms;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;

public class RenderInstance extends DataInstance implements WorldRenderEvents.Start{
    HashMap<BlockPos, RenderBuffer<ShaderPrograms.PositionColorProgram>> bufferCache;
    RenderInstance(World world, Vec3d playerPos) {
        super(world, playerPos);
    }
    @Override public void close(){
        super.close();
        if(bufferCache == null) return;
        for(RenderBuffer<?> buffer : bufferCache.values()) buffer.close();
        bufferCache = null;
    }
    
    @Override public void onStart(WorldRenderContext context) {
    
    }
}
