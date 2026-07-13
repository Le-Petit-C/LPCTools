package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.translucentShapes.LineQuad;

public class MinihudStyleRenderMethod implements IRenderMethod{
    @Override public String getNameKey() { return "minihudStyle"; }
    @Override public ICSShapeRegister getShapeRegister(boolean xrays) {
        var register = LineQuad.register(xrays);
        return (pos, color)->register.register(new LineQuad(pos, color, xrays));
    }
}
