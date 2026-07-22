package lpctools;

import lpctools.util.MathUtils;
import lpctools.util.data.SimpleSpaceOctreeMap;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.*;
import java.util.function.DoubleSupplier;

public class OctreeMapTest {
	static void main() {
		SimpleSpaceOctreeMap<Integer> octreeMap = new SimpleSpaceOctreeMap<>();
		HashMap<Vector3i, Integer> hashMap = new HashMap<>();
		ArrayList<Vector3i> puttedKeys = new ArrayList<>();
		Random random = new Random();
		for(int i = 0; i < 10000; ++i) {
			switch (random.nextInt(4)) {
				case 0, 1-> {
					Vector3i key = new Vector3i(random.nextInt(), random.nextInt(), random.nextInt());
					Integer value = random.nextInt();
					if(!hashMap.containsKey(key)) puttedKeys.add(key);
					hashMap.put(key, value);
					octreeMap.put(key, value);
				}
				case 2 -> {
					if(!puttedKeys.isEmpty()) {
						int randomIndex = random.nextInt(puttedKeys.size());
						Vector3i key = puttedKeys.get(randomIndex);
						puttedKeys.set(randomIndex, puttedKeys.getLast());
						puttedKeys.removeLast();
						if(!Objects.equals(octreeMap.remove(key), hashMap.remove(key)))
							throw new RuntimeException("1");
					}
				}
				case 3 -> {
					Vector3i key = new Vector3i(random.nextInt(), random.nextInt(), random.nextInt());
					if(!hashMap.containsKey(key))
						if(!Objects.equals(octreeMap.remove(key), hashMap.remove(key)))
							throw new RuntimeException("2");
				}
			}
		}
		System.out.println("octreeMapSize: " + octreeMap.size() + ", hashMapSize: " + hashMap.size());
		for(var key : puttedKeys) {
			if(!Objects.equals(octreeMap.get(key), hashMap.get(key)))
				throw new RuntimeException("3");
		}
		int n = 0;
		octreeMap.validate();
		DoubleSupplier gen = ()->random.nextDouble(Integer.MIN_VALUE, -(double) Integer.MIN_VALUE);
		double cx = gen.getAsDouble(), cy = gen.getAsDouble(), cz = gen.getAsDouble();
		Vector3d center = new Vector3d(cx, cy, cz);
		System.out.println("Center for fromClosest:(" + cx + "," + cy + "," + cz + ")");
		double distance = 0;
		Vector3i prevKey = null;
		for(var entry : octreeMap.fromClosest(cx, cy, cz)) {
			Vector3i key = entry.getKey(new Vector3i());
			if(!Objects.equals(entry.getValue(), hashMap.get(key)))
				throw new RuntimeException("4");
			double newDistance = MathUtils.cycledSquaredClosestDistance(center.x, center.y, center.z, key.x, key.y, key.z);
			if(newDistance < distance) {
				System.out.println("PREV: dist=" + distance + " key=(" + prevKey.x + "," + prevKey.y + "," + prevKey.z + ")");
				double prevDistFromPQ = MathUtils.cycledSquaredClosestDistance(center.x, center.y, center.z, prevKey.x, prevKey.y, prevKey.z, 1, 1, 1);
				double curDistFromPQ = MathUtils.cycledSquaredClosestDistance(center.x, center.y, center.z, key.x, key.y, key.z, 1, 1, 1);
				System.out.println("  prev 8arg=" + prevDistFromPQ + " cur 8arg=" + curDistFromPQ);
				throw new RuntimeException("5");
			}
			distance = newDistance;
			prevKey = key;
			++n;
		}
		System.out.println("Total iterated for fromClosest: " + n);

		// --- 测试 fromClampedClosest 单调性 ---
		n = 0;
		distance = 0;
		for(var entry : octreeMap.fromClosestBounds(cx, cy, cz)) {
			Vector3i key = entry.getKey(new Vector3i());
			double d = MathUtils.cycledSquaredClosestDistance(center.x, center.y, center.z, key.x, key.y, key.z, 1, 1, 1);
			if(d < distance) throw new RuntimeException("clamped not monotonic: prev=" + distance + " cur=" + d);
			distance = d;
			++n;
		}
		System.out.println("fromClampedClosest OK: " + n + " entries");

		// --- 测试 fromClosestCentered 单调性 ---
		n = 0;
		distance = 0;
		for(var entry : octreeMap.fromClosestCentered(cx, cy, cz)) {
			Vector3i key = entry.getKey(new Vector3i());
			double d = MathUtils.cycledSquaredClosestDistance(cx - 0.5, cy - 0.5, cz - 0.5, key.x, key.y, key.z);
			if(d < distance) throw new RuntimeException("centered not monotonic: prev=" + distance + " cur=" + d);
			distance = d;
			++n;
		}
		System.out.println("fromClosestCentered OK: " + n + " entries");

		// --- 测试 iterator.remove ---
		var iter = octreeMap.fromClosest(0, 0, 0).iterator();
		int removed = 0;
		while(iter.hasNext() && removed < 50) {
			var e = iter.next();
			Vector3i k = e.getKey(new Vector3i());
			if((k.x & 1) != 0) { iter.remove(); ++removed; hashMap.remove(k); }
		}
		octreeMap.validate();
		for(var key : puttedKeys) {
			if(!Objects.equals(octreeMap.get(key), hashMap.get(key)))
				throw new RuntimeException("iterator.remove mismatch");
		}
		System.out.println("iterator.remove OK: removed " + removed);

		// --- 测试极端坐标 ---
		SimpleSpaceOctreeMap<Integer> extremeMap = new SimpleSpaceOctreeMap<>();
		int[][] extremes = {{Integer.MIN_VALUE, 0, 0}, {Integer.MAX_VALUE, 0, 0}, {0, Integer.MIN_VALUE, 0},
			{0, 0, Integer.MIN_VALUE}, {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE}};
		for(int[] pos : extremes) {
			extremeMap.put(pos[0], pos[1], pos[2], pos[0] ^ pos[1] ^ pos[2]);
		}
		extremeMap.validate();
		for(int[] pos : extremes) {
			if(extremeMap.get(pos[0], pos[1], pos[2]) == null)
				throw new RuntimeException("extreme get null at (" + pos[0] + "," + pos[1] + "," + pos[2] + ")");
		}
		// 2^32 以外的浮点查询
		double huge = 1e12;
		int count = 0;
		for(var e : extremeMap.fromClosest(huge, -huge, huge * 0.3)) { ++count; }
		if(count != extremes.length) throw new RuntimeException("extreme fromClosest count=" + count);
		extremeMap.validate();
		System.out.println("extreme coords OK");

		// --- 测试 clear ---
		extremeMap.clear();
		if(extremeMap.size() != 0) throw new RuntimeException("clear size");
		if(extremeMap.get(Integer.MIN_VALUE, 0, 0) != null) throw new RuntimeException("clear get");
		count = 0;
		for(var ignored : extremeMap.fromClosest(0, 0, 0)) { ++count; }
		if(count != 0) throw new RuntimeException("clear iteration");
		extremeMap.validate();
		System.out.println("clear OK");

		// --- 测试重复 put 同一 key ---
		SimpleSpaceOctreeMap<String> dupMap = new SimpleSpaceOctreeMap<>();
		String old = dupMap.put(42, 0, 0, "first");
		if(old != null) throw new RuntimeException("dup put first");
		old = dupMap.put(42, 0, 0, "second");
		if(!"first".equals(old)) throw new RuntimeException("dup put old=" + old);
		if(!"second".equals(dupMap.get(42, 0, 0))) throw new RuntimeException("dup put get");
		if(dupMap.size() != 1) throw new RuntimeException("dup put size=" + dupMap.size());
		dupMap.validate();
		System.out.println("duplicate put OK");

		// --- 测试 forEach ---
		int[] forEachCount = new int[1];
		octreeMap.forEach((x, y, z, v) -> { forEachCount[0]++; });
		if(forEachCount[0] != octreeMap.size()) throw new RuntimeException("forEach count");
		octreeMap.validate();
		System.out.println("forEach OK: " + forEachCount[0]);

		System.out.println("ALL TESTS PASSED");
	}
}
