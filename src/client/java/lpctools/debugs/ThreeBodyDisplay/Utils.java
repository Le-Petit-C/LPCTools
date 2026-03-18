package lpctools.debugs.ThreeBodyDisplay;

import lpctools.util.DataUtils;
import org.jetbrains.annotations.Contract;
import org.joml.Vector3d;

public class Utils {
	static final Vector3d whiteLight = lightFromTemperature(new Vector3d(), 6500);
	
	static double getBrightness(Vector3d color) {
		return 0.299 * color.x + 0.587 * color.y + 0.114 * color.z;
	}
	
	static double getBrightness(double r, double g, double b) {
		return 0.299 * r + 0.587 * g + 0.114 * b;
	}
	
	static double doubleClamp(double f) {
		return 1.0 - Math.exp(-f);
	}
	
	/*static double tanhShift(double f, double shift) {
		double t = 2.0 * f - 1.0;          // tanh(2x)
		double k = Math.tanh(2.0 * shift); // tanh(2s)
		double shifted = (t + k) / (1.0 + t * k);
		return (shifted + 1.0) * 0.5;
	}*/
	
	static double getBrightness(double r, double g, double b, double k) {
		return Math.pow(0.299 * Math.pow(r, k) + 0.587 * Math.pow(g, k) + 0.114 * Math.pow(b, k), 1.0 / k);
	}
	
	static int vector3d2Color(Vector3d vector3d, double k) {
		double r = vector3d.x * k;
		double g = vector3d.y * k;
		double b = vector3d.z * k;
		double t = 1.5 / getBrightness(r, g, b, 2);
		return DataUtils.dRGB2iRGB(doubleClamp(Math.pow(r, 2.5) * t), doubleClamp(Math.pow(g, 2.5) * t), doubleClamp(Math.pow(b, 2.5) * t));
	}
	
	static double planck(double wavelengthNm, double temperature) {
		double lambda = wavelengthNm * 1e-9;
		
		double c1 = 3.741771852e-16; // 2hc^2
		double c2 = 1.438776877e-2;  // hc/k
		
		double x = c2 / (lambda * temperature);
		
		return c1 / (Math.pow(lambda, 5.0) * (Math.exp(x) - 1.0));
	}
	
	static Vector3d wavelengthToRGB(double wavelengthNm, Vector3d out) {
		double r = 0, g = 0, b = 0;
		
		if (wavelengthNm >= 380 && wavelengthNm < 440) {
			r = -(wavelengthNm - 440) / (440 - 380) * 0.2;
			g = 0.0;
			b = 1.0;
		} else if (wavelengthNm < 490) {
			r = 0.0;
			g = (wavelengthNm - 440) / (490 - 440);
			b = 1.0;
		} else if (wavelengthNm < 510) {
			r = 0.0;
			g = 1.0;
			b = -(wavelengthNm - 510) / (510 - 490);
		} else if (wavelengthNm < 580) {
			r = (wavelengthNm - 510) / (580 - 510);
			g = 1.0;
			b = 0.0;
		} else if (wavelengthNm < 645) {
			r = 1.0;
			g = -(wavelengthNm - 645) / (645 - 580);
			b = 0.0;
		} else if (wavelengthNm <= 780) {
			r = 1.0;
			g = 0.0;
			b = 0.0;
		}
		
		// --- 视锥边缘衰减（可见光两端）
		double factor;
		if (wavelengthNm < 420) {
			factor = 0.3 + 0.7 * (wavelengthNm - 380) / (420 - 380);
		} else if (wavelengthNm > 700) {
			factor = 0.3 + 0.7 * (780 - wavelengthNm) / (780 - 700);
		} else {
			factor = 1.0;
		}
		
		// --- 紫色红光补偿（L-cone副峰）
		/*double violetBoost = Math.exp(-Math.pow((wavelengthNm - 420) / 20.0, 2.0));
		r += violetBoost * 0.25;*/
		
		double l = getBrightness(r, g, b);
		
		out.set(r, g, b).mul(factor / l);
		return out;
	}
	
	@Contract("_,_->param1")
	static Vector3d lightFromTemperature(Vector3d light, double temperature) {
		light.set(0, 0, 0);
		Vector3d tmp = new Vector3d();
		double step = 5.0;
		for (double wl = 380; wl <= 780; wl += step) {
			double intensity = planck(wl, temperature);
			light.fma(intensity, wavelengthToRGB(wl, tmp));
		}
		return light.mul(step);
	}
	
	static double radiusFromMassAndAge(double mass, double age) {
		double k = Math.pow(mass, Math.exp(Math.exp(-mass * 0.1) - 1));
		return 0.832 * Math.exp(age * 0.4 * k) * k;
	}
	
	static double lightFromMassAndAge(double mass, double age) {
		double L0 = Math.pow(mass, 3.5);
		return L0 * (1.0 + 2.0 * age + 5.0 * age * age);
	}
	
	static double temperatureFromLightAndRadius(double light, double radius) {
		return 5778.0 * Math.pow(light / (radius * radius), 0.25);
	}
}
