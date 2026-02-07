package lpctools.lpcfymasaapi.render;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import lpctools.util.CachedSupplier;
import lpctools.util.javaex.QuietAutoCloseable;

public class TranslucentQuads implements QuietAutoCloseable {
	private static final CachedSupplier<TranslucentQuadsRenderInstance> renderInstance
		= new CachedSupplier<>(TranslucentQuadsRenderInstance::new);
	private final IntArrayList referredQuads = new IntArrayList();
	
	public TranslucentQuads() {}
	public void addQuad(Quad quad) {
		referredQuads.add(renderInstance.get().addQuad(quad));
	}
	@Override public void close() {
		for(int i : referredQuads) renderInstance.get().removeQuad(i);
		referredQuads.clear();
	}
}
