package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.lpcfymasaapi.render.IPositionVertex;

public class ShapeRegister<U extends Shape<? extends IPositionVertex>> {
	private final RenderInstance renderInstance;
	ShapeRegister(RenderInstance renderInstance) {
		this.renderInstance = renderInstance;
	}
	public ShapeReference register(U shape) {
		return renderInstance.addShape(shape);
	}
}
