package lpctools;

import net.minecraft.util.math.Vec3d;

import static lpctools.util.AlgorithmUtils.iterateFromClosestInDistance;

public class DebugMain {
	public static void main(String[] args) throws InterruptedException {
		double distance = 64;
		Vec3d center = new Vec3d(0, 0, 0);
		iterateFromClosestInDistanceTest(center, distance);
		Thread.sleep(3000);
		iterateFromClosestInDistanceTest(center, distance);
		Thread.sleep(3000);
		iterateFromClosestInDistanceTest(center, distance);
	}
	private static void iterateFromClosestInDistanceTest(Vec3d center, double distance) {
		int posCount = 0;
		long start = System.nanoTime();
		for(var ignored : iterateFromClosestInDistance(center, distance))
			++posCount;
		long end = System.nanoTime();
		System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
		System.out.println("posCount: " + posCount);
	}
}
