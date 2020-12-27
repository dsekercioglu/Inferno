package dsekercioglu.mega.move;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.control.InfernoProfiler;
import dsekercioglu.mega.control.InfernoTaskManager;
import robocode.AdvancedRobot;
import robocode.HitByBulletEvent;

import java.awt.*;

public class InfernoMove {

	private final WaveManager WAVE_MANAGER;
	private final SurfManager SURF_MANAGER;

	public InfernoMove(AdvancedRobot robot,
					   InfernoDataManager dataManager,
					   InfernoTaskManager taskManager,
					   InfernoProfiler profiler) {
		WAVE_MANAGER = new WaveManager(dataManager, taskManager, profiler);
		SURF_MANAGER = new SurfManager(robot, dataManager, WAVE_MANAGER, taskManager, profiler);
	}

	public void update() {
		WAVE_MANAGER.update();
		SURF_MANAGER.update();
	}

	public void onHitByBullet(HitByBulletEvent e) {
		WAVE_MANAGER.onHitByBullet(e);
	}

	public void onPaint(Graphics2D g) {
		WAVE_MANAGER.onPaint(g);
		SURF_MANAGER.onPaint(g);
	}
}
