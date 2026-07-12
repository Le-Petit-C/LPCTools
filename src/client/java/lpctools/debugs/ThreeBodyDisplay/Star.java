package lpctools.debugs.ThreeBodyDisplay;

import org.joml.Vector3d;

import java.util.Random;

class Star {
	double mass;
	double radius;
	final Vector3d light = new Vector3d();
	record StarStatus(Vector3d position, Vector3d velocity, Vector3d acceleration) {
		StarStatus() { this(new Vector3d(), new Vector3d(), new Vector3d()); }
		void set(StarStatus status) {
			this.position.set(status.position);
			this.velocity.set(status.velocity);
			this.acceleration.set(status.acceleration);
		}
	}
	StarStatus frontStatus = new StarStatus(), backStatus = new StarStatus();
	StarStatus getStatus(boolean front) { return front ? frontStatus : backStatus; }
	void swapStatus() {
		StarStatus temp = frontStatus;
		frontStatus = backStatus;
		backStatus = temp;
	}
	
	Star(java.util.Random random, RunnerDataPack dataPack) {randomize(random, dataPack);}
	
	void randomize(java.util.Random random, RunnerDataPack dataPack) {
		mass = Math.exp(random.nextGaussian() * dataPack.massDeviation());
		double age = Math.random();
		double radius = Utils.radiusFromMassAndAge(mass, age);
		this.radius = (float)radius;
		double temperature = Utils.temperatureFromLightAndRadius(Utils.lightFromMassAndAge(mass, age), radius);
		Utils.lightFromTemperature(light, temperature).div(Utils.whiteLight);
		frontStatus.position.set(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).mul(dataPack.spreadRadius());
		frontStatus.velocity.set(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).mul(dataPack.spreadSpeed());
	}
	
	static void randomizeStars(Star[] stars, Random random, RunnerDataPack dataPack, CalcCache cache) {
		for (Star star : stars) star.randomize(random, dataPack);
		normalize(stars, cache);
		updateAccelerations(stars, cache, true);
		for (Star star : stars) star.backStatus.set(star.frontStatus);
	}
	
	static class CalcCache {
		final Vector3d tmp1 = new Vector3d();
		final Vector3d tmp2 = new Vector3d();
	}

	// 中间欧拉法迭代次数
	private static final int eulerIterationTimes = 4;

	private static void updateAccelerations(Star[] stars, CalcCache cache, boolean front) {
		for (Star star : stars) star.getStatus(front).acceleration.set(0);
		for (int i = 0; i < stars.length; ++i) {
			for (int j = i + 1; j < stars.length; ++j) {
				var star1 = stars[i];
				var star2 = stars[j];
				var status1 = star1.getStatus(front);
				var status2 = star2.getStatus(front);
				status2.position.sub(status1.position, cache.tmp1);
				double dstSquareInv1 = 1.0 / cache.tmp1.lengthSquared();
				double k1 = dstSquareInv1 * Math.sqrt(dstSquareInv1);
				status1.acceleration.fma( k1 * star2.mass, cache.tmp1);
				status2.acceleration.fma(-k1 * star1.mass, cache.tmp1);
			}
		}
	}

	static void tick(Star[] stars, double dt, CalcCache cache) {
		double halfDt = dt * 0.5;
		for (var star : stars) {
			// 先“预加”上这些东西，待会迭代时就不需要每次循环都加一遍了
			star.frontStatus.position.fma(halfDt, star.frontStatus.velocity);
			star.frontStatus.velocity.fma(halfDt, star.frontStatus.acceleration);

			// 迭代初值
			star.backStatus.acceleration.set(star.frontStatus.acceleration);
		}
		for(int i = 0; i < eulerIterationTimes; ++i) {
			for (var star : stars) {
				star.frontStatus.velocity.fma(halfDt, star.backStatus.acceleration, star.backStatus.velocity);
				star.frontStatus.position.fma(halfDt, star.backStatus.velocity, star.backStatus.position);
			}
			updateAccelerations(stars, cache, false);
		}
		for (var star : stars) star.swapStatus();
	}
	
	static void normalize(Star[] stars, CalcCache cache) {
		double massSum = 0;
		cache.tmp1.set(0, 0, 0);
		cache.tmp2.set(0, 0, 0);
		for (var star : stars) {
			massSum += star.mass;
			cache.tmp1.fma(star.mass, star.frontStatus.position);
			cache.tmp2.fma(star.mass, star.frontStatus.velocity);
		}
		double k = -1.0 / massSum;
		cache.tmp1.mul(k);
		cache.tmp2.mul(k);
		for (var star : stars) {
			star.frontStatus.position.add(cache.tmp1);
			star.frontStatus.velocity.add(cache.tmp2);
		}
	}
	
	// 通过比较动能和势能判断是否超出了限制
	static boolean isOutOfRange(Star[] stars, CalcCache cache, RunnerDataPack dataPack) {
		double massSum = 0;
		for(var star : stars) massSum += star.mass;
		for (var star : stars) {
			StarStatus current = star.frontStatus;
			double posLengthSquared = current.position.lengthSquared();
			if (posLengthSquared > (double) 30000000 * 30000000) return true;
			if (posLengthSquared <= dataPack.squaredResetDistanceLimit()) continue;
			if (!(current.position.dot(current.velocity) > 0)) continue;
			Vector3d closestPos = cache.tmp1; // 一个无意义的值
			double closestDistanceSquared = Double.POSITIVE_INFINITY;
			for (var s : stars) {
				if (s == star) continue;
				double ds = s.frontStatus.position.distanceSquared(current.position);
				if(ds < closestDistanceSquared) {
					closestDistanceSquared = ds;
					closestPos = s.frontStatus.position;
				}
			}
			double othersMassSum = massSum - star.mass;
			double Ep = othersMassSum * star.mass / closestPos.distance(current.position);
			double Ek = 0.5 * star.mass * current.velocity.lengthSquared() * massSum / othersMassSum;
			if (!(Ek < Ep)) return true;
		}
		return false;
	}
}
