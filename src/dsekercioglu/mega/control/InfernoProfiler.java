package dsekercioglu.mega.control;

import java.util.*;

public class InfernoProfiler {

	private final Stack<Call> calls = new Stack<>();

	private final HashMap<String, AvgNanos> timeTaken = new HashMap<>();

	public void push(String name) {
		calls.add(new Call(name, new AvgNanos(System.nanoTime(), 1)));
	}

	public void pop() {
		Call current = calls.pop();
		current.nanoSeconds.value = System.nanoTime() - current.nanoSeconds.value;
		if (timeTaken.containsKey(current.name)) {
			AvgNanos counter = timeTaken.get(current.name);
			counter.value += current.nanoSeconds.value;
			counter.time++;
		} else {
			timeTaken.put(current.name, new AvgNanos(current.nanoSeconds.time, 1));
		}
	}

	public void printCalls() {
		List<Call> sortedCalls = new ArrayList<>();
		Set<String> names = timeTaken.keySet();
		for (String name : names) {
			sortedCalls.add(new Call(name, timeTaken.get(name)));
		}
		Collections.sort(sortedCalls);
		for (Call sortedCall : sortedCalls) {
			String out = sortedCall.name + ": " + sortedCall.nanoSeconds.value + ": (" + (sortedCall.nanoSeconds.value / sortedCall.nanoSeconds.time) + ")";
			System.out.println(out);
		}
	}

	static class AvgNanos {

		long value;
		int time;

		public AvgNanos(long value, int time) {
			this.value = value;
			this.time = time;
		}
	}

	static class Call implements Comparable<Call> {

		String name;
		AvgNanos nanoSeconds;

		public Call(String name, AvgNanos time) {
			this.name = name;
			this.nanoSeconds = time;
		}

		@Override
		public int compareTo(Call o) {
			return Long.compare(this.nanoSeconds.time, o.nanoSeconds.time);
		}
	}
}
