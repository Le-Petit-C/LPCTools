package lpctools.lpcfymasaapi.render;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import lpctools.util.javaex.QuietAutoCloseable;

import static lpctools.lpcfymasaapi.render.TranslucentQuadsRenderInstance.instance;

public class TranslucentQuads implements QuietAutoCloseable {
	private final LongArrayList referredQuads = new LongArrayList();
	
	public TranslucentQuads() {}
	public void addQuad(Quad quad, boolean clone) { referredQuads.add(instance.addQuad(clone ? new Quad(quad) : quad)); }
	public void addQuad(double baseX, double baseY, double baseZ, double uX, double uY, double uZ, double vX, double vY, double vZ, int color) {
		addQuad(new Quad(baseX, baseY, baseZ, uX, uY, uZ, vX, vY, vZ, color), false);
	}
	public void clear(){
		referredQuads.forEach(instance::removeQuad);
		referredQuads.clear();
	}
	public int size() { return referredQuads.size(); }
	public boolean isEmpty() { return referredQuads.isEmpty(); }
	public void trim() { referredQuads.trim(); }
	public void ensureCapacity(int size) { referredQuads.ensureCapacity(size); }
	@Override public void close() { clear(); }
	/*public void giveQuadsTo(TranslucentQuads translucentQuads){
		translucentQuads.referredQuads.addAll(referredQuads);
		referredQuads.clear();
	}*/
}
