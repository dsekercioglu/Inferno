package dsekercioglu.mega.utility;

import java.util.Arrays;

public class IntegerList {

	private int[] array;
	private int size;
	private int currentCapacity;

	public IntegerList(int initialCapacity) {
		currentCapacity = initialCapacity;
		array = new int[currentCapacity];
		size = 0;
	}

	public void push(int value) {
		if (size + 1 >= currentCapacity) {
			currentCapacity += currentCapacity >> 1;
			array = Arrays.copyOf(array, currentCapacity);
		}
		array[size] = value;
		size++;
	}

	public void remove(int index) {
		System.arraycopy(array, index + 1, array, index, currentCapacity - index - 1);
		size--;
	}

	public void pop() {
		size--;
	}

	public int get(int index) {
		return array[index];
	}

	public void set(int index, int value) {
		array[index] = value;
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
	}
}
