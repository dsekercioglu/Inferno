package dsekercioglu.mega.treelearn;

import java.util.List;

public class Normalize extends Node {

	private final Node NODE;

	public Normalize(Node node) {
		NODE = node;
	}

	@Override
	public List<double[]> predict(double[] dataPoint) {
		List<double[]> prediction = NODE.predict(dataPoint);
		double sum = 1e-8;
		for (double[] doubles : prediction) {
			sum += doubles[1];
		}
		for (double[] doubles : prediction) {
			doubles[1] /= sum;
		}
		return prediction;
	}

	@Override
	public void addData(double[] dataPoint, double[] guessFactor) {
		NODE.addData(dataPoint, guessFactor);
	}

	@Override
	public int k() {
		return NODE.k();
	}
}
