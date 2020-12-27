package dsekercioglu.mega.treelearn;

import dsekercioglu.mega.treelearn.Node;

import java.util.ArrayList;
import java.util.List;

public class WeightedCombine extends Node {

	private final Node[] NODES;
	private final double[] WEIGHTS;

	public WeightedCombine(double[] weights, Node... nodes) {
		NODES = nodes;
		WEIGHTS = weights;
	}

	@Override
	public List<double[]> predict(double[] dataPoint) {
		List<double[]> allResults = new ArrayList<>();
		for (int i = 0; i < NODES.length; i++) {
			Node node = NODES[i];
			List<double[]> currentResults = node.predict(dataPoint);
			for (double[] currentResult : currentResults) {
				currentResult[1] *= WEIGHTS[i];
			}
			allResults.addAll(currentResults);
		}
		return allResults;
	}

	@Override
	public void addData(double[] dataPoint, double[] guessFactor) {
		for (Node node : NODES) {
			node.addData(dataPoint, guessFactor);
		}
	}

	@Override
	public int k() {
		int k = 0;
		for (Node node : NODES) {
			k += node.k();
		}
		return k;
	}
}
