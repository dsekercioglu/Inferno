package dsekercioglu.mega.move;

import dsekercioglu.mega.InfernoUtils;

import java.util.ArrayList;
import java.util.List;

public class WaveDanger {

	private static final double SMOOTH_FACTOR = 0.16;
	private final List<double[]> GUESS_FACTORS;

	public WaveDanger(List<double[]> guessFactors) {
		GUESS_FACTORS = new ArrayList<>();
		double sumWeights = 0;
		for (double[] values : guessFactors) {
			sumWeights += values[1];
			GUESS_FACTORS.add(new double[]{values[0], values[1]});
		}
		for (double[] values : guessFactors) {
			values[1] /= sumWeights;
		}
	}

	public double getDanger(double guessFactor) {
		double danger = 0;
		for (double[] guess_factor : GUESS_FACTORS) {
			danger += guess_factor[1] / (1 + InfernoUtils.sq(guess_factor[0] - guessFactor) / SMOOTH_FACTOR);
		}
		return danger;
	}

	public double[] safeGuessFactors(double start, double end, double stepSize, int num, int steps) {
		stepSize *= SMOOTH_FACTOR;
		double diff = (end - start) / (num - 1);
		double[] guessFactors = new double[num];
		for (int i = 0; i < num; i++) {
			double point = start + diff * i;
			for (int j = 0; j < steps; j++) {
				double derivative = 0;
				for (double[] guessFactor : GUESS_FACTORS) {
					derivative += guessFactor[1] * derivatives(guessFactor[0] - point);
				}
				point = InfernoUtils.limit(point - stepSize * Math.signum(derivative), start, end);
			}
			guessFactors[i] = point;
		}
		return guessFactors;
	}

	private double derivatives(double diff) {
		double sq = diff * diff;
		return diff / (sq + 1);
	}
}
