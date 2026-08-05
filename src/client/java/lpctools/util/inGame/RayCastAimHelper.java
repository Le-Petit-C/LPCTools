package lpctools.util.inGame;

import lpctools.util.data.minecraft.MutableAABB;
import lpctools.util.data.minecraft.Vector3dEx;
import lpctools.util.data.minecraft.Vector3fEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;

import static lpctools.util.MathUtils.distanceSquared;

// RAY_CAST_ONLY 模式的共享逻辑：收集方块可见候选瞄准点，逐个试探 raycast 找到可行朝向
final class RayCastAimHelper {
	private final InGameManager manager;
	private final MutableAABB aabbCache = new MutableAABB();
	private final Vec3 playerEye;
	private final ArrayList<PositionDistance> posesCache = new ArrayList<>();
	private final Vector3dEx posCache1 = new Vector3dEx();
	private final Vector3dEx posCache2 = new Vector3dEx();

	RayCastAimHelper(InGameManager manager) {
		this.manager = manager;
		this.playerEye = manager.playerEyePos();
	}

	private record PositionDistance(double x, double y, double z, double dstSqr) {
		<T extends Vector3d> T get(T v) { v.set(x, y, z); return v; }
	}

	// 收集方块 shape 的候选瞄准点，按距离升序逐个试探 raycast；命中（hitTest 通过）则把朝向写入 res 并返回 true
	boolean findAimDirection(BlockPos pos, Predicate<BlockHitResult> hitTest, Vector3fEx res) {
		manager.getMovedBlockShape(pos).forAllBoxes((x1, y1, z1, x2, y2, z2)->{
			aabbCache.set(x1, y1, z1, x2, y2, z2).clamp(posCache1.set(playerEye));
			posCache2.setAsCenter(pos);
			VoxelShape[] shapes = new VoxelShape[3];
			for(var axis : Direction.Axis.values()) {
				BlockPos p = pos.relative(axis, posCache1.choose(axis) > posCache2.choose(axis) ? 1 : -1);
				shapes[axis.ordinal()] = manager.getMovedBlockShape(p);
			}
			outerLoop:
			for(int i = 0; i < 8; ++i) {
				double x = (i & 1) == 0 ? posCache1.x : posCache2.x;
				double y = (i & 2) == 0 ? posCache1.y : posCache2.y;
				double z = (i & 4) == 0 ? posCache1.z : posCache2.z;
				for (var shape : shapes) {
					boolean[] shouldContinue = {false};
					shape.forAllBoxes((_x1, _y1, _z1, _x2, _y2, _z2) -> {
						if (aabbCache.set(_x1, _y1, _z1, _x2, _y2, _z2).inflateAndSet(0.000001).contains(x, y, z))
							shouldContinue[0] = true;
					});
					if (shouldContinue[0]) continue outerLoop;
				}
				posesCache.add(new PositionDistance(x, y, z, distanceSquared(playerEye, x, y, z)));
			}
		});
		posesCache.sort(Comparator.comparingDouble(v->v.dstSqr));
		float oldYRotRaw = manager.getYRotRaw(), oldXRotRaw = manager.getXRotRaw();
		float oldYRot = manager.getYRot(), oldXRot = manager.getXRot();
		boolean bRes = false;
		for(var p : posesCache) {
			p.get(posCache1).sub(playerEye, res);
			manager.setRot(res, oldYRot, oldXRot);
			if(manager.raycastHitResult() instanceof BlockHitResult blockHit && hitTest.test(blockHit)) {
				bRes = true;
				break;
			}
		}
		manager.setRotRaw(oldYRotRaw, oldXRotRaw);
		posesCache.clear();
		return bRes;
	}
}
