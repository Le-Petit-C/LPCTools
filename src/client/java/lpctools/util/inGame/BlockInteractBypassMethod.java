package lpctools.util.inGame;

import lpctools.util.data.minecraft.MutableAABB;
import lpctools.util.data.minecraft.Vector3dEx;
import lpctools.util.data.minecraft.Vector3fEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;

import static lpctools.util.MathUtils.*;

public enum BlockInteractBypassMethod implements BlockOperationRunner.CalculatorGenerator<BlockInteractBypassMethod.StatusCalculator> {
	NONE {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final Vec3 playerEyePos = manager.playerEyePos();
				final Vec3 playerViewVec = manager.playerViewVector();
				final MutableAABB aabbCache = new MutableAABB();
				@Override public BlockHitResult getValidInteractHitResult(BlockPos targetPos, @Nullable Direction targetDirection) {
					Vec3 location = aabbCache.setFullCube(targetPos).clamp(playerEyePos);
					return new BlockHitResult(location,
						targetDirection == null ? possibleHitDirection(targetPos, playerEyePos, playerViewVec) : targetDirection,
						targetPos.immutable(), aabbCache.contains(playerEyePos));
				}
				@Override public boolean getBlockInteractDirection(BlockPos targetPos, @Nullable Direction targetDirection, Vector3fEx res) { return false; }
			};
		}
	},
	// 需要玩家朝向方块
	DIRECTION {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final double reach = manager.blockInteractionRange();
				final double reachSqr = square(reach);
				final Vec3 playerEyePos = manager.playerEyePos();
				final Vec3 playerViewVec = manager.playerViewVector();
				final MutableAABB aabbCache = new MutableAABB();
				final Vector3dEx posCache = new Vector3dEx();
				@Override public @Nullable BlockHitResult getValidInteractHitResult(BlockPos targetPos, @Nullable Direction targetDirection) {
					OptionalDouble rayCastResult = aabbCache.setFullCube(targetPos).rayCastDistance(playerEyePos, playerViewVec);
					if(rayCastResult.isPresent() && 0 <= rayCastResult.getAsDouble() && rayCastResult.getAsDouble() < reach) {
						Vec3 location = aabbCache.clamp(playerEyePos);
						return new BlockHitResult(location,
							targetDirection != null ? targetDirection : possibleHitDirection(targetPos, playerEyePos, playerViewVec),
							targetPos.immutable(), aabbCache.contains(playerEyePos));
					}
					return null;
				}
				@Override public boolean getBlockInteractDirection(BlockPos targetPos, @Nullable Direction targetDirection, Vector3fEx res) {
					if(minSquaredDistanceToBlock(playerEyePos, targetPos) >= reachSqr) return false;
					aabbCache.setFullCube(targetPos).clamp(posCache.set(playerEyePos)).sub(playerEyePos, res);
					if(res.lengthSquared() <= 0) res.set(playerViewVec);
					return true;
				}
			};
		}
	},
	// 仅允许使用raycastHitResult
	RAY_CAST_ONLY {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final RayCastAimHelper aimHelper = new RayCastAimHelper(manager);
				@Override public @Nullable BlockHitResult getValidInteractHitResult(BlockPos targetPos, @Nullable Direction targetDirection) {
					return manager.raycastHitResult() instanceof BlockHitResult blockHitResult
						&& blockHitResult.getBlockPos().equals(targetPos)
						&& (targetDirection == null || targetDirection == blockHitResult.getDirection())
						? blockHitResult : null;
				}
				@Override public boolean getBlockInteractDirection(BlockPos targetPos, @Nullable Direction targetDirection, Vector3fEx res) {
					return aimHelper.findAimDirection(targetPos, blockHit -> blockHit.getBlockPos().equals(targetPos)
						&& (targetDirection == null || targetDirection == blockHit.getDirection()), res);
				}
			};
		}
	};
	// blockPlace的targetDirection为放在哪个方向的方块上，玩家朝向需求由BlockPlacing处理
	public interface StatusCalculator {
		@Nullable BlockHitResult getValidInteractHitResult(BlockPos targetPos, @Nullable Direction targetDirection);
		boolean getBlockInteractDirection(BlockPos targetPos, @Nullable Direction targetDirection, Vector3fEx res);
	}
}
