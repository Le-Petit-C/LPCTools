package lpctools.util.inGame;

import lpctools.util.data.minecraft.MutableAABB;
import lpctools.util.data.minecraft.Vector3dEx;
import lpctools.util.data.minecraft.Vector3fEx;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.OptionalDouble;

import static lpctools.util.MathUtils.square;

// 实体交互/攻击共用的绕过判定：判断玩家能否"够到/选中"某个实体
public enum EntityBypassMethod implements InGameOperationRunner.CalculatorGenerator<EntityBypassMethod.StatusCalculator> {
	NONE {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final double reachSqr = square(manager.entityInteractionRange());
				final Vec3 playerEye = manager.playerEyePos();
				@Override public boolean isReachable(Entity entity) { return entity.distanceToSqr(playerEye) < reachSqr; }
				@Override public boolean getAimDirection(Entity entity, Vector3fEx res) { return false; }
			};
		}
	},
	// 需要玩家朝向实体（视线射线与实体包围盒相交）
	DIRECTION {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final double reach = manager.entityInteractionRange();
				final Vec3 playerEye = manager.playerEyePos();
				final Vec3 playerView = manager.playerViewVector();
				final MutableAABB aabbCache = new MutableAABB();
				final Vector3dEx posCache = new Vector3dEx();
				@Override public boolean isReachable(Entity entity) {
					OptionalDouble rd = aabbCache.set(entity.getBoundingBox()).rayCastDistance(playerEye, playerView);
					return rd.isPresent() && 0 <= rd.getAsDouble() && rd.getAsDouble() < reach;
				}
				@Override public boolean getAimDirection(Entity entity, Vector3fEx res) {
					aabbCache.set(entity.getBoundingBox()).clamp(posCache.set(playerEye)).sub(playerEye, res);
					if(res.lengthSquared() <= 0) res.set(playerView);
					return true;
				}
			};
		}
	},
	// 仅使用真实的 raycast 命中（准星瞄准实体）
	RAY_CAST_ONLY {
		@Override public StatusCalculator createCalculator(InGameManager manager) {
			return new StatusCalculator() {
				final Vec3 playerEye = manager.playerEyePos();
				final MutableAABB aabbCache = new MutableAABB();
				final Vector3dEx posCache1 = new Vector3dEx();
				final Vector3dEx posCache2 = new Vector3dEx();
				@Override public boolean isReachable(Entity entity) {
					return manager.raycastHitResult() instanceof EntityHitResult ehr && ehr.getEntity() == entity;
				}
				@Override public boolean getAimDirection(Entity entity, Vector3fEx res) {
					// 实体包围盒是单个 AABB：clamp 点 + bbox 中心生成 8 个候选点，逐个转头测试
					aabbCache.set(entity.getBoundingBox());
					aabbCache.clamp(posCache1.set(playerEye));
					posCache2.set(entity.getBoundingBox().getCenter());
					ArrayList<Candidate> candidates = new ArrayList<>();
					for(var z : new double[]{posCache1.z, posCache2.z})
						for(var y : new double[]{posCache1.y, posCache2.y})
							for(var x : new double[]{posCache1.x, posCache2.x})
								candidates.add(new Candidate(x, y, z, entity.distanceToSqr(x, y, z)));
					candidates.sort(Comparator.comparingDouble(c -> c.dstSqr));
					float oldYRotRaw = manager.getYRotRaw(), oldXRotRaw = manager.getXRotRaw();
					float oldYRot = manager.getYRot(), oldXRot = manager.getXRot();
					boolean bRes = false;
					for(var c : candidates) {
						c.get(posCache1).sub(playerEye, res);
						manager.setRot(res, oldYRot, oldXRot);
						if(manager.raycastHitResult() instanceof EntityHitResult ehr && ehr.getEntity() == entity) {
							bRes = true;
							break;
						}
					}
					manager.setRotRaw(oldYRotRaw, oldXRotRaw);
					return bRes;
				}
			};
		}
	};

	record Candidate(double x, double y, double z, double dstSqr) {
		<T extends Vector3d> T get(T v) { v.set(x, y, z); return v; }
	}

	public interface StatusCalculator {
		boolean isReachable(Entity entity);
		boolean getAimDirection(Entity entity, Vector3fEx res);
	}
}
