package lpctools.lpcfymasaapi.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import fi.dy.masa.malilib.render.MaLiLibPipelines;
import lpctools.util.javaex.QuietAutoCloseable;

import java.util.ArrayList;

public class TranslucentShapes implements QuietAutoCloseable {
	
	private static TranslucentShapesRenderInstance getRenderInstance(RenderPipeline pipeline){
		return TranslucentShapesRenderInstance.getRenderInstance(pipeline);
	}
	
	public static RenderPipeline shapePipeline(){
		return MaLiLibPipelines.POSITION_COLOR_MASA;
		//return MaLiLibPipelines.POSITION_COLOR_MASA_DEPTH_MASK;
	}
	
	public static RenderPipeline linePipeline(){
		return MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE;
	}
	
	private static TranslucentShapesRenderInstance shapeInstance(){
		return getRenderInstance(shapePipeline());
	}
	private static TranslucentShapesRenderInstance lineInstance(){
		return getRenderInstance(linePipeline());
	}
	
	private final TranslucentShapesRenderInstance renderInstance;
	
	private final ArrayList<ShapeReference> referredShapes = new ArrayList<>();
	
	TranslucentShapes(TranslucentShapesRenderInstance renderInstance) { this.renderInstance = renderInstance; }
	@SuppressWarnings("unused") public TranslucentShapes(RenderPipeline renderPipeline) { this(TranslucentShapesRenderInstance.getRenderInstance(renderPipeline)); }
	public TranslucentShapes(boolean isLine) { this(isLine ? lineInstance() : shapeInstance()); }
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
