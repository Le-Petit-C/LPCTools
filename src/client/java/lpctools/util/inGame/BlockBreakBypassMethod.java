package lpctools.util.inGame;

import lpctools.util.MathUtils;
import lpctools.util.data.minecraft.MutableAABB;
import lpctools.util.data.minecraft.Vector3dEx;
import lpctools.util.data.minecraft.Vector3fEx;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalDouble;

public enum BlockBreakBypassMethod implements BlockOperationRunner.CalculatorGenerator<BlockBreakBypassMethod.StatusCalculator> {
	NONE {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final Vec3 playerEye = manager.playerEyePos();
				final Vec3 playerView = manager.playerViewVector();
				@Override public @Nullable Direction getHitDirection(BlockPos pos) { return MathUtils.possibleHitDirection(pos, playerEye, playerView); }
				@Override public boolean getTargetDirection(BlockPos pos, Vector3fEx res) { return false; }
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
	RAY_CAST_ONLY {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final RayCastAimHelper aimHelper = new RayCastAimHelper(manager);
				@Override public @Nullable Direction getHitDirection(BlockPos pos) {
					return manager.raycastHitResult() instanceof BlockHitResult blockHitResult
						&& blockHitResult.getBlockPos().equals(pos) ? blockHitResult.getDirection() : null;
				}
				@Override public boolean getTargetDirection(BlockPos pos, Vector3fEx res) {
					return aimHelper.findAimDirection(pos, blockHit -> blockHit.getBlockPos().equals(pos), res);
				}
			};
		}
	};
	public interface StatusCalculator {
		@Nullable Direction getHitDirection(BlockPos pos);
		boolean getTargetDirection(BlockPos pos, Vector3fEx res);
	}
}
