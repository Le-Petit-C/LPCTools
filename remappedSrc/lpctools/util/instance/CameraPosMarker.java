package lpctools.util.instance;

import lpctools.LPCTools;

import java.security.InvalidParameterException;

// 解决“切换FreeCamera状态时一些已标记的内容会被意外清理，可能是因为切换FreeCamera的一瞬间Camera的坐标在世界原点”的问题
// 期望的解决方法是不直接清理当前摄像机位置一定距离外的而是清理距离能包含最近几次摄像机位置的最小圆一定距离外的
public class CameraPosMarker {
	private final int count;
	private final double[] x, z;
	private int i = 0;
	private boolean isCalculated = true;
	private double resX = 0, resZ = 0, resR = 0;
	public CameraPosMarker() { this(4); }
	public CameraPosMarker(int count) {
		if(count <= 0) throw new InvalidParameterException("count must be positive");
		if(count > 10) LPCTools.LOGGER.warn("lpctools.LPCTools.CameraPosMarker : Too many camera points to record!");
		this.count = count;
		x = new double[count];
		z = new double[count];
	}
	public void nextPos(double x, double z) {
		this.x[i] = x;
		this.z[i] = z;
		if(++i >= count) i = 0;
		isCalculated = false;
	}
	public double getResX() {
		calculateResult();
		return resX;
	}
	public double getResZ() {
		calculateResult();
		return resZ;
	}
	public  double getResR() {
		calculateResult();
		return resR;
	}
	private void calculateResult() {
		if (isCalculated) return;
		isCalculated = true;
		
		double bestX = 0;
		double bestZ = 0;
		double bestR = Double.POSITIVE_INFINITY;
		
		// 两点直径圆
		for (int a = 0; a < count; a++) {
			for (int b = a + 1; b < count; b++) {
				
				double cx = (x[a] + x[b]) * 0.5;
				double cz = (z[a] + z[b]) * 0.5;
				double r = dist(cx, cz, x[a], z[a]);
				
				if (r >= bestR) continue;
				if (containsAll(cx, cz, r)) {
					bestX = cx;
					bestZ = cz;
					bestR = r;
				}
			}
		}
		
		// 三点外接圆
		for (int a = 0; a < count; a++) {
			for (int b = a + 1; b < count; b++) {
				for (int c = b + 1; c < count; c++) {
					
					double[] circle = circleFrom3(x[a], z[a], x[b], z[b], x[c], z[c]);
					if (circle == null) continue;
					
					double cx = circle[0];
					double cz = circle[1];
					double r = circle[2];
					
					if (r >= bestR) continue;
					if (containsAll(cx, cz, r)) {
						bestX = cx;
						bestZ = cz;
						bestR = r;
					}
				}
			}
		}
		
		// 单点情况
		if (bestR == Double.POSITIVE_INFINITY) {
			bestX = x[0];
			bestZ = z[0];
			bestR = 0;
		}
		
		resX = bestX;
		resZ = bestZ;
		resR = bestR;
	}
	
	private boolean containsAll(double cx, double cz, double r) {
		double r2 = r * r + 1e-9;
		for (int i = 0; i < count; i++) {
			double dx = x[i] - cx;
			double dz = z[i] - cz;
			if (dx * dx + dz * dz > r2) return false;
		}
		return true;
	}
	
	private static double dist(double x1, double z1, double x2, double z2) {
		double dx = x1 - x2;
		double dz = z1 - z2;
		return Math.sqrt(dx * dx + dz * dz);
	}
	
	private static double[] circleFrom3(double ax, double az,
										double bx, double bz,
										double cx, double cz) {
		
		double a = bx - ax;
		double b = bz - az;
		double c = cx - ax;
		double d = cz - az;
		
		double e = a * (ax + bx) + b * (az + bz);
		double f = c * (ax + cx) + d * (az + cz);
		double g = 2 * (a * (cz - bz) - b * (cx - bx));
		
		if (Math.abs(g) < 1e-12) return null;
		
		double centerX = (d * e - b * f) / g;
		double centerZ = (a * f - c * e) / g;
		double r = dist(centerX, centerZ, ax, az);
		
		return new double[]{centerX, centerZ, r};
	}
}
