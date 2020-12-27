package dsekercioglu.mega.radar;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.math.Vec2;
import robocode.AdvancedRobot;
import robocode.util.Utils;

import java.awt.*;

public class InfernoRadar {

	private final AdvancedRobot ROBOT;
	private final InfernoDataManager DATA_MANAGER;

	public InfernoRadar(AdvancedRobot robot, InfernoDataManager dataManager) {
		ROBOT = robot;
		DATA_MANAGER = dataManager;
	}

	public void run() {
		while (true) {
			ROBOT.turnRadarRightRadians(Double.POSITIVE_INFINITY);
			ROBOT.scan();
		}
	}

	public void update() {
		double turnAmt = Utils.normalRelativeAngle(DATA_MANAGER.getAbsoluteBearing(0) - DATA_MANAGER.getBotRadarHeading());
		ROBOT.setTurnRadarRightRadians(turnAmt + Math.signum(turnAmt) * Math.PI / 18);
	}

	public void onPaint(Graphics2D g) {
		Vec2 enemyPosition = DATA_MANAGER.getCurrentEnemyPosition();
		g.drawRect((int) (enemyPosition.getX() - 17.5), (int) (enemyPosition.getY() - 17.5), 36, 36);
	}
}
