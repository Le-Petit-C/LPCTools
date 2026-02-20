package lpctools.lpcfymasaapi.render.translucentShapes;

import lpctools.util.javaex.QuietAutoCloseable;

public interface ShapeReference extends QuietAutoCloseable {
	void removeShape();
	
	@Override default void close() {removeShape();}
}
