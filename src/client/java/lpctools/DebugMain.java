package lpctools;

import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static lpctools.util.AlgorithmUtils.iterateFromClosestInDistance;

public class DebugMain {
	public static void main(String[] args) throws InterruptedException {
		double distance = 64;
		Vec3d center = new Vec3d(0.5, 0.5, 0.5);
		iterateFromClosestInDistanceTest(center, distance);
		Thread.sleep(1000);
		iterateFromClosestInDistanceTest(center, distance);
		Thread.sleep(1000);
		iterateFromClosestInDistanceTest(center, distance);
		Thread.sleep(1000);
		oldIterateFromClosestInDistanceTest(center, distance);
		Thread.sleep(1000);
		oldIterateFromClosestInDistanceTest(center, distance);
		Thread.sleep(1000);
		oldIterateFromClosestInDistanceTest(center, distance);
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
	private static void oldIterateFromClosestInDistanceTest(Vec3d center, double distance) {
		int posCount = 0;
		long start = System.nanoTime();
		for(var ignored : AlgorithmUtils.iterateFromClosestInDistance(center, distance))
			++posCount;
		long end = System.nanoTime();
		System.out.println("Time: " + (end - start) / 1_000_000.0 + " ms");
		System.out.println("posCount: " + posCount);
	}
	
	public static class AlgorithmUtils {
		public static Iterable<BlockPos> iterateFromClosestInDistance(Vec3d center, double distance){
			return () -> new InClosestIterator3D(center, distance);
		}
		
		public static class ClosestIterator3D implements Iterator<BlockPos>{
			private final @NotNull Vec3d center;
			protected final PriorityQueue<BlockPos> poses;
			protected final HashSet<BlockPos> posSet;
			ClosestIterator3D(@NotNull Vec3d center){
				this.center = center;
				poses = new PriorityQueue<>(
					(o1, o2) -> {
						double v = getSquaredDistance(o1) - getSquaredDistance(o2);
						return v == 0 ? 0 : (v < 0 ? -1 : 1);
					}
				);
				BlockPos pos = BlockPos.ofFloored(center);
				for (int x = 0; x < 2; ++x)
					for (int y = 0; y < 2; ++y)
						for (int z = 0; z < 2; ++z) {
							var newPos = pos.add(x, y, z);
							poses.add(newPos);
						}
				posSet = new HashSet<>(poses);
			}
			public double getSquaredDistance(BlockPos pos) {
				return pos.getSquaredDistance(center);
			}
			public double getNextDistance(){
				assert !poses.isEmpty();
				return getSquaredDistance(poses.peek());
			}
			@Override public boolean hasNext() {return true;}
			@Override public BlockPos next() {
				assert !poses.isEmpty();
				BlockPos ret = poses.remove();
				posSet.remove(ret);
				for (Direction direction : Direction.values()) {
					BlockPos nextPos = ret.offset(direction);
					if (getSquaredDistance(nextPos) <= getSquaredDistance(ret)) continue;
					if (posSet.contains(nextPos)) continue;
					posSet.add(nextPos);
					poses.add(nextPos);
				}
				return ret;
			}
		}
		public static class InClosestIterator3D extends ClosestIterator3D {
			public final double maxSquaredDistance;
			InClosestIterator3D(@NotNull Vec3d center, double maxDistance) {
				super(center);
				maxSquaredDistance = maxDistance * maxDistance;
			}
			@Override public boolean hasNext() {return getNextDistance() <= maxSquaredDistance;}
		}
		
	}
	
}
