package lpctools.util;

import org.jetbrains.annotations.Contract;

@SuppressWarnings("unused")
public class LPCMathHelper {
	// 为避免将z传参到y被IDEA报警告，此处使用x1,x2...而不是xyz
	@Contract(pure = true)
	public static double squaredLength(double x1, double x2){
		return x1 * x1 + x2 * x2;
	}
	@Contract(pure = true)
	public static double squaredLength(double x1, double x2, double x3){
		return x1 * x1 + x2 * x2 + x3 * x3;
	}
	@Contract(pure = true)
	public static double squaredLength(double x1, double x2, double x3, double... otherCoords){
		double sum = x1 * x1 + x2 * x2 + x3 * x3;
		for(double coord : otherCoords) sum += coord * coord;
		return sum;
	}
}
