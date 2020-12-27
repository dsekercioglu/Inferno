package dsekercioglu.mega.treelearn;

import dsekercioglu.mega.jk.KDTree;
import dsekercioglu.mega.treelearn.Node;

import java.util.ArrayList;
import java.util.List;

//TODO: Separate into classes specialized for certain amount of inputs.
public class ProfileN extends Node {

	private static final double COEFF = 25;

	private final int DIMS;

	private final int[] DIMENSIONS;
	private final double[] WEIGHTS;
	private final int K;

	private final KDTree.Manhattan<double[]> KD_TREE;

	public ProfileN(int[] dims, double[] weights, int k) {
		DIMS = dims.length;
		DIMENSIONS = dims;
		WEIGHTS = weights;
		double sumWeights = 0;
		for (int dim : dims) {
			sumWeights += weights[dim];
		}
		for (int i = 0; i < WEIGHTS.length; i++) {
			WEIGHTS[i] /= sumWeights;
		}
		K = k;
		KD_TREE = new KDTree.Manhattan<>(DIMS);
	}

	@Override
	public List<double[]> predict(double[] dataPoint) {
		List<KDTree.SearchResult<double[]>> searchResults = KD_TREE.nearestNeighbours(getDataPoint(dataPoint), K);
		List<double[]> guessFactorsWeights;
		if (searchResults.isEmpty()) {
			guessFactorsWeights = new ArrayList<>(1);
			guessFactorsWeights.add(new double[]{0, 1});
		} else {
			guessFactorsWeights = new ArrayList<>(searchResults.size());
			for (KDTree.SearchResult<double[]> searchResult : searchResults) {
				double[] weightedSearchResult = {
						searchResult.payload[0],
						searchResult.payload[1] / (1 + searchResult.distance * COEFF)
				};
				guessFactorsWeights.add(weightedSearchResult);
			}
		}
		return guessFactorsWeights;
	}

	@Override
	public void addData(double[] dataPoint, double[] guessFactor) {
		KD_TREE.addPoint(
				getDataPoint(dataPoint),
				guessFactor
		);
	}

	@Override
	public int k() {
		return K;
	}

	private double[] getDataPoint(double[] dataPoint) {
		double[] processedData = new double[DIMS];
		for (int i = 0; i < DIMS; i++) {
			processedData[i] = dataPoint[DIMENSIONS[i]] * WEIGHTS[i];
		}
		return processedData;
	}
}
