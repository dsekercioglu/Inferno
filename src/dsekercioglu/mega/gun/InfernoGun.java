package dsekercioglu.mega.gun;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.InfernoUtils;
import dsekercioglu.mega.control.InfernoProfiler;
import dsekercioglu.mega.control.InfernoTaskManager;
import robocode.AdvancedRobot;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class InfernoGun {

	private static final double STD_FIRE_POWER = 2;

	private final AdvancedRobot ROBOT;
	private final InfernoDataManager DATA_MANAGER;
	private final InfernoTaskManager TASK_MANAGER;
	private final InfernoProfiler PROFILER;
	private final WaveTracker WAVE_TRACKER;

	private static final int BIN_NUM = 51;
	private static final int MID_BIN = 25;
	private final double[] BINS = new double[BIN_NUM];

	public InfernoGun(AdvancedRobot robot,
					  InfernoDataManager dataManager,
					  InfernoTaskManager taskManager,
					  InfernoProfiler profiler) {
		ROBOT = robot;
		DATA_MANAGER = dataManager;
		TASK_MANAGER = taskManager;
		PROFILER = profiler;
		WAVE_TRACKER = new WaveTracker(DATA_MANAGER);
	}

	public void run() {
		WAVE_TRACKER.run();
	}

	public void update() {
		WAVE_TRACKER.update();
		double firePower = firePower();
		WAVE_TRACKER.addWave(firePower);
		aim:
		{
			if (DATA_MANAGER.getBotGunHeat() < 0.4) {
				if (TASK_MANAGER.allowTask(InfernoTaskManager.TARGET_PREDICT)) {
					PROFILER.push("Aim");
					handleAim(firePower);
					PROFILER.pop();
					break aim;
				}
			}
			ROBOT.setTurnGunRightRadians(Utils.normalRelativeAngle(DATA_MANAGER.getAbsoluteBearing(0) - DATA_MANAGER.getBotGunHeading()));
		}
	}

	private void handleAim(double firePower) {
		ROBOT.setTurnGunRightRadians(Utils.normalRelativeAngle(DATA_MANAGER.getAbsoluteBearing(0) + fireAngle(firePower) - DATA_MANAGER.getBotGunHeading()));
		ROBOT.setFire(firePower);
	}

	private double firePower() {
		return Math.min(STD_FIRE_POWER, DATA_MANAGER.getBotEnergy());
	}

	private double fireAngle(double firePower) {
		List<double[]> searchResults = WAVE_TRACKER.predict(firePower);
		Arrays.fill(BINS, 0);

		int bestBin = 0;
		for (double[] searchResult : searchResults) {
			int bin = (int) InfernoUtils.limit(((searchResult[0] + 1) * MID_BIN), 0, BIN_NUM - 1);
			BINS[bin] += searchResult[1];
			if (BINS[bin] > BINS[bestBin]) {
				bestBin = bin;
			}
		}
		return ((bestBin * 1D / MID_BIN) - 1) * DATA_MANAGER.getEnemyLateralDirection(0) * InfernoUtils.mea(Rules.getBulletSpeed(firePower));
	}

	public void onPaint(Graphics2D g) {
		//WAVE_TRACKER.onPaint(g);
	}
}
