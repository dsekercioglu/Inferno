package dsekercioglu.mega.utility;

import java.util.Arrays;

public class DoubleCycler {


	private final double[] ARRAY;
	private final int CAPACITY;
	private int offset = 0;

	public DoubleCycler(int capacity) {
		this.CAPACITY = capacity;
		ARRAY = new double[capacity];
	}

	public void update(double d) {
		ARRAY[offset] = d;
		offset = (offset + 1) % CAPACITY;
	}

	public double get(int past) {
		return ARRAY[(offset + CAPACITY - 1 - past) % CAPACITY];
	}

	public void clear() {
		offset = 0;
		zero();
	}

	public void zero() {
		Arrays.fill(ARRAY, 0);
	}
}
