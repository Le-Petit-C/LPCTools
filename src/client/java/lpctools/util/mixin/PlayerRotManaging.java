package lpctools.util.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import lpctools.util.MathUtils;
import lpctools.util.inGame.InGameManager;
import lpctools.util.javaex.QuietAutoCloseable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3fc;

import static lpctools.generic.Bypassing.*;
import static lpctools.util.MathUtils.*;

public class PlayerRotManaging {
	private static float targetYRot, targetXRot;
	private static boolean hasTargetRot = false;
	private static boolean rotModifiedLastTick = false;

	public static void setTargetRot(float YRot, float XRot) {
		targetYRot = modToCenter(YRot, Mth.TWO_PI);
		targetXRot = Math.clamp(XRot, -Mth.HALF_PI, Mth.HALF_PI);
		hasTargetRot = true;
	}

	public static class CloserRotSet implements QuietAutoCloseable {
		private final float playerYRot, playerXRot;
		private final float targetDistSqr = (float)square(maxRotateSpeed.getAsDouble());
		private float currentDistSqr = Float.POSITIVE_INFINITY;
		private float targetYRot, targetXRot;
		private CloserRotSet(float playerYRot, float playerXRot) {
			this.playerYRot = playerYRot;
			this.playerXRot = playerXRot;
		}
		public void setIfCloser(float YRot, float XRot) {
			float distSqr = rotDistanceSquared(playerYRot, playerXRot, YRot, XRot);
			boolean shouldExchange;
			if(distSqr < targetDistSqr) {
				if(currentDistSqr < targetDistSqr) shouldExchange = distSqr > currentDistSqr;
				else shouldExchange = true;
			}
			else {
				if(currentDistSqr < targetDistSqr) shouldExchange = false;
				else shouldExchange = distSqr < currentDistSqr;
			}
			if(shouldExchange) {
				currentDistSqr = distSqr;
				targetYRot = YRot;
				targetXRot = XRot;
			}
		}

		public void setIfCloser(Vector3fc target) {
			float YRot = MathUtils.YRotOrDefault(target.x(), target.y(), target.z(), playerYRot);
			float XRot = MathUtils.XRotOrDefault(target.x(), target.y(), target.z(), playerXRot);
			setIfCloser(YRot, XRot);
		}

		@Override public void close() {
			if(Float.isFinite(currentDistSqr)) setTargetRotIfCloser(playerYRot, playerXRot, targetYRot, targetXRot);
		}
	}

	public static CloserRotSet closerRotSet(float playerYRot, float playerXRot) { return new CloserRotSet(playerYRot, playerXRot); }
	public static CloserRotSet closerRotSet(InGameManager manager) { return closerRotSet(manager.yRotLast(), manager.xRotLast()); }

	public static void setTargetRotIfCloser(float playerYRot, float playerXRot, float YRot, float XRot) {
		float targetYRot = modToCenter(YRot, Mth.TWO_PI);
		float targetXRot = Math.clamp(XRot, -Mth.HALF_PI, Mth.HALF_PI);
		if(!hasTargetRot || MathUtils.rotDistanceSquared(playerYRot, playerXRot, targetYRot, targetXRot)
			< MathUtils.rotDistanceSquared(playerYRot, playerXRot, PlayerRotManaging.targetYRot, PlayerRotManaging.targetXRot)
		) setTargetRot(YRot, XRot);
	}

	public static void setTargetRotIfCloser(InGameManager manager, float YRot, float XRot) {
		setTargetRotIfCloser(manager.yRotLast(), manager.xRotLast(), YRot, XRot);
	}

	public static void setTargetRotIfCloser(InGameManager manager, Vector3fc target) {
		float YRot = MathUtils.YRotOrDefault(target.x(), target.y(), target.z(), manager.yRotLast());
		float XRot = MathUtils.XRotOrDefault(target.x(), target.y(), target.z(), manager.xRotLast());
		PlayerRotManaging.setTargetRotIfCloser(manager, YRot, XRot);
	}

	@ApiStatus.Internal
	public static void wrapSendPositionInTick(LocalPlayer instance, Operation<Void> original, float yRotLast, float xRotLast) {
		if(!rotModifiedLastTick && !hasTargetRot)
			original.call(instance);
		else {
			float oldYRot = instance.getYRot();
			float oldXRot = instance.getXRot();
			float targetYRotDegrees, targetXRotDegrees;
			if (hasTargetRot) {
				targetYRotDegrees = PlayerRotManaging.targetYRot * (180 / Mth.PI);
				targetXRotDegrees = PlayerRotManaging.targetXRot * (180 / Mth.PI);
				rotModifiedLastTick = true;
			} else {
				targetYRotDegrees = oldYRot;
				targetXRotDegrees = oldXRot;
			}
			float dYRot = modToCenter(targetYRotDegrees - yRotLast, 360), dXRot = targetXRotDegrees - xRotLast;
			float rotDistance = Mth.sqrt(dYRot * dYRot + dXRot * dXRot) * (Mth.PI / 180);
			float maxRotSpeed = (float) maxRotateSpeed.getDoubleValue();
			if(rotDistance > maxRotSpeed) {
				float k = maxRotSpeed / rotDistance;
				targetYRotDegrees = Math.fma(k, dYRot, yRotLast);
				targetXRotDegrees = Math.fma(k, dXRot, xRotLast);
			}
			else if(!hasTargetRot) rotModifiedLastTick = false;
			instance.setYRot(targetYRotDegrees);
			instance.setXRot(targetXRotDegrees);
			try {
				original.call(instance);
			} finally {
				instance.setYRot(oldYRot);
				instance.setXRot(oldXRot);
			}
			hasTargetRot = false;
		}
	}
}
