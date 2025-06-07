package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.gl.furtherWarpped.RenderBuffer;
import lpctools.shader.ShaderPrograms;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public class RenderInstance implements AutoCloseable{
    HashMap<BlockPos, RenderBuffer<ShaderPrograms.PositionColorProgram>> bufferCache;
    RenderInstance(){
    
    }
    @Override public void close() throws Exception {
        if(bufferCache == null) return;
        for(RenderBuffer<?> buffer : bufferCache.values()) buffer.close();
        bufferCache = null;
    }
}
