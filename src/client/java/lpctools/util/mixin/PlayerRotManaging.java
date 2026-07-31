package lpctools.util.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import lpctools.generic.Bypassing;
import lpctools.util.MathUtils;
import lpctools.util.inGame.InGameManager;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3fc;

import static lpctools.util.MathUtils.*;

public class PlayerRotManaging {
	private static float targetXRot, targetYRot;
	private static boolean hasTargetRot = false;
	private static boolean rotModifiedLastTick = false;

	public static void setTargetRot(float YRot, float XRot) {
		targetYRot = modToCenter(YRot * (180 / Mth.PI), 360);
		targetXRot = Math.clamp(XRot * (180 / Mth.PI), -90.0f, 90.0f);
		hasTargetRot = true;
	}

	public static void setTargetRotIfCloser(InGameManager manager, float YRot, float XRot) {
		float playerYRotLast = manager.yRotLast();
		float playerXRotLast = manager.xRotLast();
		float targetYRot = modToCenter(YRot, Mth.TWO_PI);
		float targetXRot = Math.clamp(XRot, -Mth.HALF_PI, Mth.HALF_PI);
		if(!hasTargetRot || MathUtils.rotDistanceSquared(playerYRotLast, playerXRotLast, targetYRot, targetXRot)
			< MathUtils.rotDistanceSquared(playerYRotLast, playerXRotLast, PlayerRotManaging.targetYRot, PlayerRotManaging.targetXRot)
		) setTargetRot(YRot, XRot);
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
			float targetYRot, targetXRot;
			if (hasTargetRot) {
				targetYRot = PlayerRotManaging.targetYRot;
				targetXRot = PlayerRotManaging.targetXRot;
				rotModifiedLastTick = true;
			} else {
				targetYRot = oldYRot;
				targetXRot = oldXRot;
			}
			float dYRot = modToCenter(targetYRot - yRotLast, 360), dXRot = targetXRot - xRotLast;
			float rotDistance = Mth.sqrt(dYRot * dYRot + dXRot * dXRot) * (Mth.PI / 180);
			float maxRotSpeed = (float) Bypassing.maxRotateSpeed.getDoubleValue();
			if(rotDistance > maxRotSpeed) {
				float k = maxRotSpeed / rotDistance;
				targetYRot = Math.fma(k, dYRot, yRotLast);
				targetXRot = Math.fma(k, dXRot, xRotLast);
			}
			else if(!hasTargetRot) rotModifiedLastTick = false;
			instance.setYRot(targetYRot);
			instance.setXRot(targetXRot);
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
