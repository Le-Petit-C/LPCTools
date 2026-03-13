package lpctools.tools.canSpawnDisplay;

import lpctools.lpcfymasaapi.render.translucentShapes.LineCube;

public class LineCubeRenderMethod implements IRenderMethod {
	@Override public String getNameKey() { return "lineCube"; }
	
	@Override public ICSShapeRegister getShapeRegister(boolean xrays) {
		var register = LineCube.register(xrays);
		return (pos, color)->register.register(new LineCube(pos, color));
	}
}
