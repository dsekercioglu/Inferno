package dsekercioglu.mega.control;

import dsekercioglu.mega.InfernoUtils;
import dsekercioglu.mega.utility.DoubleCycler;
import dsekercioglu.mega.utility.DoubleList;

import java.awt.*;

public class InfernoTimer {

	private double lastTime;
	private final double ROLL_FACTOR;
	private final DoubleList TIME;

	public InfernoTimer(double rollFactor) {
		ROLL_FACTOR = rollFactor;
		TIME = new DoubleList(10);
	}

	public void add(long nanoTime) {
		lastTime = lastTime * ROLL_FACTOR + nanoTime * (1 - ROLL_FACTOR);
		TIME.push(lastTime);
	}

	public void graph(Graphics2D g, int startX, int startY, int endX, int endY) {
		double mean = 0;
		int chunks = (int) Math.sqrt(TIME.size());
		for (int i = 0; i < TIME.size(); i += chunks) {
			int iter = Math.min(TIME.size() - i - 1, chunks);
			double currentMean = 0;
			for (int j = 0; j < iter; j++) {
				currentMean += TIME.get(i + j) / iter;
			}
			mean += currentMean / chunks;
		}
		double variance = 0;
		for (int i = 0; i < TIME.size(); i++) {
			variance += InfernoUtils.sq(mean - TIME.get(i)) / TIME.size();
		}
		double factor = Math.sqrt(variance) * 2 / (endY - endX) * 4;

		double midPoint = (startY + endY) * 0.5D;
		double diff = endX - startX;
		int prevX = endX;
		int prevY = endY;
		g.setColor(Color.RED);
		int start = Math.max(0, TIME.size() - 1000);
		for (int i = start; i < TIME.size(); i++) {
			int x = (int) (endX - (i - start) * diff / 999 + 0.5);
			int y = (int) (midPoint + (TIME.get(i) - mean) / factor + 0.5);
			g.drawLine(prevX, prevY, x, y);
			prevX = x;
			prevY = y;
		}
	}
}
