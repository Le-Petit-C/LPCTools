package lpctools.debugs.ThreeBodyDisplay;

public record RunnerDataPack(
	double maxTrackSpeed,
	double squaredResetDistanceLimit,
	double timeSpeed,
	double spreadRadius,
	double spreadSpeed,
	double massDeviation,
	float starRadiusFactor)
{}
