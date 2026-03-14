package lpctools.lpcfymasaapi.render.translucentShapes;

public enum TranslateMethod {
	// 一般的投影矩阵-模型视图矩阵-矫正平移方案
	PROJECTION__MODEL_VIEW__OFFSET(Location.PROJECTION, Location.OFFSET),
	// 将offset合并进MODEL_VIEW的方案
	PROJECTION__MODEL_VIEW(Location.PROJECTION, Location.MODEL_VIEW),
	// 将projection Translation提取到MODEL_VIEW的方案
	PROJECTION__BIASED_MODEL_VIEW__OFFSET(Location.MODEL_VIEW, Location.OFFSET),
	// 将projection Translation提取到OFFSET的方案
	PROJECTION__MODEL_VIEW__BIASED_OFFSET(Location.OFFSET, Location.OFFSET)
	;
	public final Location projectionTranslationLocation;
	public final Location offsetLocation;
	
	TranslateMethod(Location projectionTranslationLocation, Location offsetLocation) {
		this.projectionTranslationLocation = projectionTranslationLocation;
		this.offsetLocation = offsetLocation;
	}
	
	public enum Location{
		PROJECTION,
		MODEL_VIEW,
		OFFSET
	}
}
