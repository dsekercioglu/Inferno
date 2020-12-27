package dsekercioglu.mega.utility;

import dsekercioglu.mega.math.Vec2;

import java.util.Arrays;

public class Vec2Cycler {


	private final Vec2[] ARRAY;
	private final int CAPACITY;
	private int offset = 0;

	public Vec2Cycler(int capacity) {
		this.CAPACITY = capacity;
		ARRAY = new Vec2[capacity];
		for (int i = 0; i < ARRAY.length; i++) {
			ARRAY[i] = new Vec2();
		}
	}

	public void update(Vec2 vec2) {
		ARRAY[offset].setLocation(vec2);
		offset = (offset + 1) % CAPACITY;
	}

	public Vec2 get(int past) {
		return ARRAY[(offset + CAPACITY - 1 - past) % CAPACITY];
	}

	public void clear() {
		offset = 0;
	}

	public void zero() {
		for (int i = 0; i < ARRAY.length; i++) {
			ARRAY[i] = new Vec2();
		}
	}
}
