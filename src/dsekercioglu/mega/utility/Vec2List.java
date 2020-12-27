package dsekercioglu.mega.utility;

import dsekercioglu.mega.math.Vec2;

import java.util.Arrays;

public class Vec2List {

	private Vec2[] array;
	private int size;
	private int currentCapacity;

	public Vec2List(int initialCapacity) {
		currentCapacity = initialCapacity;
		array = new Vec2[currentCapacity];
		size = 0;
	}

	public void push(Vec2 vec2) {
		if (size + 1 >= currentCapacity) {
			currentCapacity += currentCapacity >> 1;
			array = Arrays.copyOf(array, currentCapacity);
		}
		array[size] = vec2;
		size++;
	}

	public void remove(int index) {
		System.arraycopy(array, index + 1, array, index, currentCapacity - index - 1);
		size--;
	}

	public void pop() {
		size--;
	}

	public Vec2 get(int index) {
		return array[index];
	}

	public void set(int index, Vec2 vec2) {
		array[index] = vec2;
	}

	public int size() {
		return size;
	}

	public void clear() {
		size = 0;
	}
}
