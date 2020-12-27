package dsekercioglu.mega;

import dsekercioglu.mega.utility.DoubleList;
import dsekercioglu.mega.math.FastTrig;

public class InfernoUtils {

	public static double mea(double bulletVelocity) {
		return FastTrig.asin(8 / bulletVelocity);
	}

	public static double sq(double val) {
		return val * val;
	}

	public static double cb(double val) {
		return val * val * val;
	}

	public static double limit(double val, double min, double max) {
		return val > max ? max : Math.max(val, min);
	}

	public static double mean(DoubleList list) {
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i);
		}
		return sum / list.size();
	}

	public static double variance(DoubleList list, double mean) {
		double variance = 0;
		for (int i = 0; i < list.size(); i++) {
			variance += sq(mean - list.get(i));
		}
		return variance / list.size();
	}
}
