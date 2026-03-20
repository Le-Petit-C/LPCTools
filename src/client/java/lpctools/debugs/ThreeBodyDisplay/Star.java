package lpctools.debugs.ThreeBodyDisplay;

import org.joml.Vector3d;

import java.util.Random;

class Star {
	double mass;
	double radius;
	final Vector3d light = new Vector3d();
	final Vector3d position = new Vector3d(), velocity = new Vector3d(), vChange = new Vector3d();
	
	Star(java.util.Random random, RunnerDataPack dataPack) {randomize(random, dataPack);}
	
	void randomize(java.util.Random random, RunnerDataPack dataPack) {
		mass = Math.exp(random.nextGaussian() * dataPack.massDeviation());
		double age = Math.random();
		double radius = Utils.radiusFromMassAndAge(mass, age);
		this.radius = (float)radius;
		double temperature = Utils.temperatureFromLightAndRadius(Utils.lightFromMassAndAge(mass, age), radius);
		Utils.lightFromTemperature(light, temperature).div(Utils.whiteLight);
		// LPCTools.LOGGER.info("temperature:{}light:({},{},{})", temperature, light.x, light.y, light.z);
		// color = 0xff7fafff;
		position.set(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).mul(dataPack.spreadRadius());
		velocity.set(random.nextGaussian(), random.nextGaussian(), random.nextGaussian()).mul(dataPack.spreadSpeed());
	}
	
	static void randomizeStars(Star[] stars, Random random, RunnerDataPack dataPack, CalcCache cache) {
		for (Star star : stars) star.randomize(random, dataPack);
		normalize(stars, cache);
	}
	
	static class CalcCache {
		final Vector3d tmp1 = new Vector3d();
		final Vector3d tmp2 = new Vector3d();
	}
	
	static void tick(Star[] stars, double dt, CalcCache cache) {
		for (var star : stars) star.vChange.set(0, 0, 0);
		for (int i = 0; i < stars.length; ++i) {
			for (int j = i + 1; j < stars.length; ++j) {
				var star1 = stars[i];
				var star2 = stars[j];
				star2.position.sub(star1.position, cache.tmp1);
				double dstSquareInv = 1.0 / cache.tmp1.lengthSquared();
				cache.tmp1.mul(dstSquareInv * Math.sqrt(dstSquareInv) * dt);
				star1.vChange.add(cache.tmp1.mul(star2.mass, cache.tmp2));
				star2.vChange.sub(cache.tmp1.mul(star1.mass, cache.tmp2));
			}
		}
		for (var star : stars) {
			star.position.fma(dt, star.velocity).fma(0.5 * dt, star.vChange);
			star.velocity.add(star.vChange);
		}
	}
	
	static void normalize(Star[] stars, CalcCache cache) {
		double massSum = 0;
		cache.tmp1.set(0, 0, 0);
		cache.tmp2.set(0, 0, 0);
		for (var star : stars) {
			massSum += star.mass;
			cache.tmp1.fma(star.mass, star.position);
			cache.tmp2.fma(star.mass, star.velocity);
		}
		double k = -1.0 / massSum;
		cache.tmp1.mul(k);
		cache.tmp2.mul(k);
		for (var star : stars) {
			star.position.add(cache.tmp1);
			star.velocity.add(cache.tmp2);
		}
	}
	
	// 通过比较动能和势能判断是否超出了限制
	static boolean isOutOfRange(Star[] stars, CalcCache cache, RunnerDataPack dataPack) {
		double massSum = 0;
		for(var star : stars) massSum += star.mass;
		for (var star : stars) {
			double posLengthSquared = star.position.lengthSquared();
			if (posLengthSquared > (double) 30000000 * 30000000) return true;
			if (posLengthSquared <= dataPack.squaredResetDistanceLimit()) continue;
			if (!(star.position.dot(star.velocity) > 0)) continue;
			Vector3d closestPos = cache.tmp1; // 一个无意义的值
			double closestDistanceSquared = Double.POSITIVE_INFINITY;
			for (var s : stars) {
				if (s == star) continue;
				double ds = s.position.distanceSquared(star.position);
				if(ds < closestDistanceSquared) {
					closestDistanceSquared = ds;
					closestPos = s.position;
				}
			}
			double othersMassSum = massSum - star.mass;
			double Ep = othersMassSum * star.mass / closestPos.distance(star.position);
			double Ek = 0.5 * star.mass * star.velocity.lengthSquared() * massSum / othersMassSum;
			if (!(Ek < Ep)) return true;
		}
		return false;
	}
}
