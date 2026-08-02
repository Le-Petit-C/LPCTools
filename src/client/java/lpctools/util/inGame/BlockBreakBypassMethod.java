package lpctools.util.inGame;

import lpctools.util.MathUtils;
import lpctools.util.data.minecraft.MutableAABB;
import lpctools.util.data.minecraft.Vector3dEx;
import lpctools.util.data.minecraft.Vector3fEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.OptionalDouble;

public enum BlockBreakBypassMethod implements BlockOperationRunner.CalculatorGenerator<BlockBreakBypassMethod.StatusCalculator> {
	NONE {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final double reachSqr = MathUtils.square(manager.blockInteractionRange());
				final Vec3 playerEye = manager.playerEyePos();
				final Vec3 playerView = manager.playerViewVector();
				@Override public @Nullable Direction getHitDirection(BlockPos pos) {
					return MathUtils.minSquaredDistanceToBlock(playerEye, pos) < reachSqr ? MathUtils.possibleHitDirection(pos, playerEye, playerView) : null;
				}
				@Override public boolean getTargetDirection(BlockPos pos, Vector3fEx res) {
					res.set(playerView);
					return true;
				}
			};
		}
	},
	DIRECTION {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final double reach = manager.blockInteractionRange();
				final MutableAABB aabbCache = new MutableAABB();
				final Vec3 playerEye = manager.playerEyePos();
				final Vec3 playerView = manager.playerViewVector();
				final Vector3dEx posCache = new Vector3dEx();
				@Override public @Nullable Direction getHitDirection(BlockPos pos) {
					aabbCache.setFullCube(pos);
					OptionalDouble rayCastResult = aabbCache.rayCastDistance(playerEye, playerView);
					if(rayCastResult.isPresent() && 0 <= rayCastResult.getAsDouble() && rayCastResult.getAsDouble() < reach)
						return MathUtils.possibleHitDirection(pos, playerEye, playerView);
					else return null;
				}
				@Override public boolean getTargetDirection(BlockPos pos, Vector3fEx res) {
					aabbCache.setFullCube(pos).clamp(posCache.set(playerEye)).sub(playerEye, res);
					if(res.lengthSquared() <= 0) res.set(playerView);
					return true;
				}
			};
		}
	},
	ONLY_RAY_CAST {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final MutableAABB aabbCache = new MutableAABB();
				final Vec3 playerEye = manager.playerEyePos();
				final ArrayList<PositionDistance> posesCache = new ArrayList<>();
				final Vector3dEx posCache1 = new Vector3dEx();
				final Vector3dEx posCache2 = new Vector3dEx();
				record PositionDistance(double x, double y, double z, double dstSqr) {
					<T extends Vector3d> T get(T v) { v.set(x, y, z); return v; }
				}
				@Override public @Nullable Direction getHitDirection(BlockPos pos) {
					return manager.raycastHitResult() instanceof BlockHitResult blockHitResult
						&& blockHitResult.getBlockPos().equals(pos) ? blockHitResult.getDirection() : null;
				}
				boolean testVoxelShape(double x, double y, double z, VoxelShape shape) {
					boolean[] res = { false };
					shape.forAllBoxes((x1, y1, z1, x2, y2, z2)->{
						if(aabbCache.set(x1, y1, z1, x2, y2, z2).inflateAndSet(0.000001).contains(x, y, z))
							res[0] = true;
					});
					return res[0];
				}
				void tryAddPos(double x, double y, double z, VoxelShape bx, VoxelShape by, VoxelShape bz) {
					if(testVoxelShape(x, y, z, bx) || testVoxelShape(x, y, z, by) || testVoxelShape(x, y, z, bz)) return;
					posesCache.add(new PositionDistance(x, y, z, MathUtils.distanceSquared(playerEye, x, y, z)));
				}
				@Override public boolean getTargetDirection(BlockPos pos, Vector3fEx res) {
					manager.getBlockState(pos).getShape(manager.level, pos).forAllBoxes((x1, y1, z1, x2, y2, z2)->{
						aabbCache.set(x1, y1, z1, x2, y2, z2).clamp(posCache1.set(playerEye));
						posCache2.setAsCenter(pos);
						BlockPos px = pos.relative(Direction.fromAxisAndDirection(Direction.Axis.X, posCache1.x > posCache2.x ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE));
						BlockPos py = pos.relative(Direction.fromAxisAndDirection(Direction.Axis.Y, posCache1.y > posCache2.y ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE));
						BlockPos pz = pos.relative(Direction.fromAxisAndDirection(Direction.Axis.Z, posCache1.z > posCache2.z ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE));
						VoxelShape bx = manager.getBlockState(px).getShape(manager.level, px);
						VoxelShape by = manager.getBlockState(py).getShape(manager.level, py);
						VoxelShape bz = manager.getBlockState(pz).getShape(manager.level, pz);
						tryAddPos(posCache1.x, posCache1.y, posCache1.z, bx, by, bz);
						tryAddPos(posCache2.x, posCache1.y, posCache1.z, bx, by, bz);
						tryAddPos(posCache1.x, posCache2.y, posCache1.z, bx, by, bz);
						tryAddPos(posCache2.x, posCache2.y, posCache1.z, bx, by, bz);
						tryAddPos(posCache1.x, posCache1.y, posCache2.z, bx, by, bz);
						tryAddPos(posCache2.x, posCache1.y, posCache2.z, bx, by, bz);
						tryAddPos(posCache1.x, posCache2.y, posCache2.z, bx, by, bz);
						tryAddPos(posCache2.x, posCache2.y, posCache2.z, bx, by, bz);
					});
					posesCache.sort(Comparator.comparingDouble(v->v.dstSqr));
					float oldYRotRaw = manager.getYRotRaw(), oldXRotRaw = manager.getXRotRaw();
					float oldYRot = manager.getYRot(), oldXRot = manager.getXRot();
					boolean bRes = false;
					for(var p : posesCache) {
						p.get(posCache1).sub(playerEye, res);
						manager.setRot(res, oldYRot, oldXRot);
						if(manager.raycastHitResult() instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(pos)) {
							bRes = true;
							break;
						}
					}
					manager.setRotRaw(oldYRotRaw, oldXRotRaw);
					posesCache.clear();
					return bRes;
				}
			};
		}
	};
	public interface StatusCalculator {
		@Nullable Direction getHitDirection(BlockPos pos);
		boolean getTargetDirection(BlockPos pos, Vector3fEx res);
	}
}
