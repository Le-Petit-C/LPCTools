package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.util.javaex.QuietAutoCloseable;

import java.util.ArrayList;

public class TranslucentShapes implements QuietAutoCloseable {
	private final RenderInstance renderInstance;
	
	private final ArrayList<ShapeReference> referredShapes = new ArrayList<>();
	
	TranslucentShapes(RenderInstance renderInstance) { this.renderInstance = renderInstance; }
	@SuppressWarnings("unused") public TranslucentShapes(RenderOption renderOption) { this(RenderInstance.getRenderInstance(renderOption)); }
	public TranslucentShapes(boolean isLine, boolean depthless) { this(RenderInstance.defaultRenderInstance(isLine, depthless)); }
	public void addQuad(Quad quad, boolean clone) { referredShapes.add(renderInstance.addShape(clone ? new Quad(quad) : quad)); }
	public void addQuad(double baseX, double baseY, double baseZ, double uX, double uY, double uZ, double vX, double vY, double vZ, int color) {
		addQuad(new Quad(baseX, baseY, baseZ, uX, uY, uZ, vX, vY, vZ, color), false);
	}
	public void clear(){
		referredShapes.forEach(ShapeReference::close);
		referredShapes.clear();
	}
	public int size() { return referredShapes.size(); }
	public boolean isEmpty() { return referredShapes.isEmpty(); }
	public void trim() { referredShapes.trimToSize(); }
	public void ensureCapacity(int size) { referredShapes.ensureCapacity(size); }
	@Override public void close() { clear(); }
}
