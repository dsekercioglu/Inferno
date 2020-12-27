package dsekercioglu.mega.move;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.InfernoUtils;
import dsekercioglu.mega.control.InfernoProfiler;
import dsekercioglu.mega.control.InfernoTaskManager;
import dsekercioglu.mega.math.Vec2;
import robocode.AdvancedRobot;
import robocode.util.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class SurfManager {

	private static final double WALL_STICK_LENGTH = 120;
	private static final double PREFERRED_DISTANCE = 450;
	private static final double AGGRESSION = 1.2;

	private final AdvancedRobot ROBOT;
	private final InfernoDataManager DATA_MANAGER;
	private final InfernoTaskManager TASK_MANAGER;
	private final InfernoProfiler PROFILER;
	private final WaveManager WAVE_MANAGER;
	private final Rectangle2D BATTLEFIELD;

	private final Vec2 clockwiseMove = new Vec2(0, 0);
	private final Vec2 counterClockwiseMove = new Vec2(0, 0);

	public SurfManager(AdvancedRobot robot,
					   InfernoDataManager dataManager,
					   WaveManager waveManager,
					   InfernoTaskManager taskManager,
					   InfernoProfiler profiler) {
		ROBOT = robot;
		DATA_MANAGER = dataManager;
		WAVE_MANAGER = waveManager;
		TASK_MANAGER = taskManager;
		PROFILER = profiler;
		BATTLEFIELD = new Rectangle2D.Double(
				18,
				18,
				DATA_MANAGER.BATTLE_FIELD_WIDTH - 36,
				DATA_MANAGER.BATTLE_FIELD_HEIGHT - 36
		);
	}

	public void update() {
		PROFILER.push("Surf update");
		int wave = getClosestWave();
		Vec2 source;
		double[] velDistTime;
		if (wave != -1) {
			source = WAVE_MANAGER.getSource().get(wave);
			velDistTime = WAVE_MANAGER.getVelDistTime().get(wave);
		} else {
			source = DATA_MANAGER.getCurrentEnemyPosition();
			velDistTime = new double[]{14, 0, 0};
		}
		predict(
				DATA_MANAGER.getCurrentRobotPosition(),
				DATA_MANAGER.getHeading(0),
				DATA_MANAGER.getVelocity(0),
				source,
				velDistTime[2],
				velDistTime[0],
				1,
				clockwiseMove
		);
		predict(
				DATA_MANAGER.getCurrentRobotPosition(),
				DATA_MANAGER.getHeading(0),
				DATA_MANAGER.getVelocity(0),
				source,
				velDistTime[2],
				velDistTime[0],
				-1,
				counterClockwiseMove
		);
		if (getDanger(clockwiseMove) < getDanger(counterClockwiseMove)) {
			move(source, 1);
		} else {
			move(source, -1);
		}
		PROFILER.pop();
	}

	private double getDanger(Vec2 position) {
		double waveDanger = WAVE_MANAGER.getDanger(position);
		return BATTLEFIELD.contains(position) ? (waveDanger == 0 ? 1 : waveDanger) / position.distanceSq(DATA_MANAGER.getCurrentEnemyPosition()) : Double.POSITIVE_INFINITY;
	}

	private void move(Vec2 source, double dir) {
		double heading = DATA_MANAGER.getHeading(0);
		double targetHeading = getSmoothedTargetHeading(DATA_MANAGER.getCurrentRobotPosition(), source, dir);
		double turnAmount = Utils.normalRelativeAngle(targetHeading - heading);
		double moveDir = 1;
		if (Math.abs(turnAmount) > Math.PI / 2) {
			turnAmount += Math.PI;
			moveDir = -1;
		}
		ROBOT.setAhead(moveDir * 36);
		ROBOT.setTurnRightRadians(Utils.normalRelativeAngle(turnAmount));
	}

	private void predict(Vec2 position,
						 double heading,
						 double velocity,
						 Vec2 waveSource,
						 double waveTime,
						 double waveVelocity,
						 double dir,
						 Vec2 out) {
		out.setLocation(position);
		while (waveTime < 91) {
			double targetHeading = getSmoothedTargetHeading(out, waveSource, dir);
			double maxTurn = Math.toRadians(10 - 0.75 * Math.abs(velocity));
			double turnAmount = Utils.normalRelativeAngle(targetHeading - heading);
			double moveDir = 1;
			if (Math.abs(turnAmount) > Math.PI / 2) {
				turnAmount += Math.PI;
				moveDir = -1;
			}
			heading += InfernoUtils.limit(
					Utils.normalRelativeAngle(turnAmount),
					-maxTurn,
					maxTurn
			);
			velocity = InfernoUtils.limit(velocity + (velocity * moveDir < 0 ? 2 * moveDir : moveDir), -8, 8);
			out.project(Vec2.direction(heading), velocity);
			if (InfernoUtils.sq(waveVelocity * (++waveTime)) > waveSource.distanceSq(out)) {
				return;
			}
		}
	}


	private double getSmoothedTargetHeading(Vec2 position, Vec2 source, double dir) {
		return wallSmooth(
				position,
				targetHeading(
						position,
						source,
						dir
				),
				dir * 0.05);
	}

	private double targetHeading(Vec2 position, Vec2 waveSource, double moveDirection) {
		double targetHeading = waveSource.angleTo(position) + moveDirection * Math.PI / 2;
		targetHeading += (waveSource.distance(position) - PREFERRED_DISTANCE) / PREFERRED_DISTANCE * moveDirection * AGGRESSION;
		return targetHeading;
	}

	private double wallSmooth(Vec2 position, double heading, double orientation) {
		if (!BATTLEFIELD.contains(Vec2.project(position, Vec2.direction(heading), WALL_STICK_LENGTH))) {
			heading += orientation;
			int safetyCounter = 0;
			while (!BATTLEFIELD.contains(Vec2.project(position, Vec2.direction(heading), WALL_STICK_LENGTH)) && safetyCounter < 100) {
				heading += orientation;
				safetyCounter++;
			}
		}
		return heading;
	}

	private int getClosestWave() {
		List<double[]> velDistTime = WAVE_MANAGER.getVelDistTime();
		double highestDistanceTraveled = 0;
		int index = -1;
		for (int i = 0; i < velDistTime.size(); i++) {
			double[] currentVelDistTime = velDistTime.get(i);
			if (currentVelDistTime[1] > highestDistanceTraveled) {
				highestDistanceTraveled = currentVelDistTime[1];
				index = i;
			}
		}
		return index;
	}

	public void onPaint(Graphics2D g) {
		//g.setColor(Color.RED);
	}
}
