package dsekercioglu.mega;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.control.InfernoProfiler;
import dsekercioglu.mega.control.InfernoTimer;
import dsekercioglu.mega.control.InfernoTaskManager;
import dsekercioglu.mega.gun.InfernoGun;
import dsekercioglu.mega.math.FastTrig;
import dsekercioglu.mega.move.InfernoMove;
import dsekercioglu.mega.radar.InfernoRadar;
import robocode.*;

import java.awt.*;

public class Inferno extends AdvancedRobot {

	private static InfernoProfiler profiler;
	private static InfernoDataManager dataManager;
	private static InfernoTaskManager taskManager;
	private static InfernoTimer timer;
	private static InfernoRadar radar;
	private static InfernoMove move;
	private static InfernoGun gun;

	static int skippedTurns = 0;

	static {
		FastTrig.init();
	}

	public void run() {

		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);

		setBodyColor(new Color(195, 0, 0));
		setGunColor(new Color(225, 0, 0));
		setRadarColor(Color.RED);
		setScanColor(Color.RED);
		setBulletColor(Color.WHITE);
		if (dataManager == null) {
			profiler = new InfernoProfiler();
			dataManager = new InfernoDataManager(this);
			taskManager = new InfernoTaskManager();
			timer = new InfernoTimer(0.999);
			radar = new InfernoRadar(this, dataManager);
			move = new InfernoMove(this, dataManager, taskManager, profiler);
			gun = new InfernoGun(this, dataManager, taskManager, profiler);
		}
		dataManager.run();
		gun.run();
		radar.run();
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		long time = System.nanoTime();
		taskManager.update();
		dataManager.onScannedRobot(e);
		radar.update();
		move.update();
		gun.update();
		taskManager.lateUpdate();
		timer.add(System.nanoTime() - time);
	}

	public void onRoundEnded(RoundEndedEvent e) {
		profiler.printCalls();
	}

	public void onHitByBullet(HitByBulletEvent e) {
		move.onHitByBullet(e);
	}

	public void onPaint(Graphics2D g) {
		radar.onPaint(g);
		gun.onPaint(g);
		move.onPaint(g);
		timer.graph(g, 0, 0, 800, 100);
	}

	public void onSkippedTurn(SkippedTurnEvent e) {
		skippedTurns++;
	}
}
