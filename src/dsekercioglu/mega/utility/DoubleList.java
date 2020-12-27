package dsekercioglu.mega.utility;

import java.util.Arrays;

public class DoubleList {

	private double[] array;
	private int size;
	private int currentCapacity;

	public DoubleList(int initialCapacity) {
		currentCapacity = initialCapacity;
		array = new double[currentCapacity];
		size = 0;
	}

	public void push(double d) {
		if (size + 1 >= currentCapacity) {
			currentCapacity += currentCapacity >> 1;
			array = Arrays.copyOf(array, currentCapacity);
		}
		array[size] = d;
		size++;
	}

	public void remove(int index) {
		System.arraycopy(array, index + 1, array, index, currentCapacity - index - 1);
		size--;
	}

	public void pop() {
		size--;
	}

	public double get(int index) {
		return array[index];
	}

	public void set(int index, double value) {
		array[index] = value;
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
	}
}
