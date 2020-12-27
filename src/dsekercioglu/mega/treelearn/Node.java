package dsekercioglu.mega.treelearn;

import java.util.List;

public abstract class Node {

	public abstract List<double[]> predict(double[] dataPoint);

	public abstract void addData(double[] dataPoint, double[] guessFactor);

	public abstract int k();
}
