package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.translucentShapes.*;

public class FullSurfaceRenderMethod implements IRenderMethod{
    @Override public String getNameKey() {return "fullSurface";}
    
    @Override public ICSShapeRegister getShapeRegister(boolean xrays) {
        var register = Quad.register(xrays);
        return (pos, color)->register.register(
            new Quad(pos.getX(), pos.getY(), pos.getZ(), 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, color, false));
    }
}
