package lpctools.lpcfymasaapi.render.translucentShapes;

public enum TranslateMethod {
	// 将offset合并进MODEL_VIEW的方案
	PROJECTION__MODEL_VIEW(Location.PROJECTION, Location.MODEL_VIEW),
	;
	public final Location projectionTranslationLocation;
	public final Location offsetLocation;
	
	TranslateMethod(Location projectionTranslationLocation, Location offsetLocation) {
		this.projectionTranslationLocation = projectionTranslationLocation;
		this.offsetLocation = offsetLocation;
	}
	
	public enum Location{
		PROJECTION,
		MODEL_VIEW
	}
}
