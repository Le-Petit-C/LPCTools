package lpctools.lpcfymasaapi.render;

public enum OffsetMode {
	NONE(0.0f, 0.0f),
	OFFSET_1(-0.3f, -0.6f),
	OFFSET_2(-0.4f, -0.8f),
	OFFSET_3(-3.0f, -3.0f);
	public final float depthBiasScaleFactor, depthBiasConstant;

	OffsetMode(float depthBiasScaleFactor, float depthBiasConstant) {
		this.depthBiasScaleFactor = depthBiasScaleFactor;
		this.depthBiasConstant = depthBiasConstant;
	}

	float depthBiasScaleFactor() {
		return depthBiasScaleFactor;
	}

	float depthBiasConstant() {
		return depthBiasConstant;
	}
}
