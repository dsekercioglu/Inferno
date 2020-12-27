package dsekercioglu.mega.move;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.InfernoUtils;
import dsekercioglu.mega.math.FastTrig;
import dsekercioglu.mega.math.Vec2;
import robocode.util.Utils;

import java.awt.geom.Rectangle2D;

import static robocode.util.Utils.normalRelativeAngle;

public class PathGen {

	private static final double WALL_STICK_LENGTH = 130;
	private static final double ROBOT_MAX_VELOCITY = 8;

	private static final double[] STOP_DISTANCE = {0, 1, 2, 4, 6, 9, 12, 16, 20};
	private static final double[] STOP_TIME = {0, 1, 1, 2, 2, 3, 3, 4, 4};

	private final InfernoDataManager DATA_MANAGER;
	private final Rectangle2D.Double BATTLEFIELD;
	private final int MAX_PATHS;
	private final int PATH_LENGTH;
	private final Vec2[] POSITIONS;
	private final double[][] STATES;
	private final int[] PATH_BREAK;

	public PathGen(InfernoDataManager dataManager, int maximumPaths, int maxLength) {
		DATA_MANAGER = dataManager;
		MAX_PATHS = maximumPaths;
		PATH_LENGTH = maxLength;
		POSITIONS = new Vec2[maximumPaths * maxLength];
		STATES = new double[maximumPaths * maxLength][2];
		for (int i = 0; i < POSITIONS.length; i++) {
			POSITIONS[i] = new Vec2();
			STATES[i] = new double[2];
		}
		PATH_BREAK = new int[maximumPaths];

		BATTLEFIELD = new Rectangle2D.Double(
				18,
				18,
				DATA_MANAGER.BATTLE_FIELD_WIDTH - 36,
				DATA_MANAGER.BATTLE_FIELD_HEIGHT - 36
		);
	}

	public void generateOnIndex(Vec2 source,
								double waveDistance,
								double waveVelocity,
								Vec2 position,
								double velocity,
								double heading,
								double targetDistance,
								double targetAngle,
								int pathIndex) {

		Vec2 predicted = position.copy();
		int step = 0;
		int index = pathIndex * PATH_LENGTH;

		double currentDistance = source.distance(predicted);
		double currentAngle = source.angleTo(predicted);
		double requiredOrbit = Utils.normalRelativeAngle(targetAngle - currentAngle);

		//TODO: Solve the actual equation for proper retreat targetHeading
		double retreatAmount = (targetDistance - currentDistance) * 0.5;
		double orbitDistance = requiredOrbit * currentDistance;
		double offset = Math.atan2(orbitDistance, retreatAmount);
		while (step < PATH_LENGTH) {
			//double orbitDistance = requiredOrbit * (retreatAmount * (Math.log(requiredOrbit * (targetDistance)) * Math.log(requiredOrbit * retreatAmount)) - currentDistance) / (currentDistance * currentDistance);

			currentDistance = source.distance(predicted);
			currentAngle = source.angleTo(predicted);
			double targetHeading = wallSmooth(predicted, currentAngle + offset, Math.signum(orbitDistance) * 0.05);

			double estimatedTimeLeft = (currentDistance - waveDistance) / (waveVelocity - Math.cos(currentAngle - targetHeading) * ROBOT_MAX_VELOCITY);
			double targetVelocity = STOP_TIME[(int) Math.abs(velocity)] < estimatedTimeLeft ? ROBOT_MAX_VELOCITY : 0;
			//double targetVelocity = ROBOT_MAX_VELOCITY;
			if (FastTrig.cos(targetHeading - heading) < 0) {
				targetHeading += Math.PI;
				targetVelocity *= -1;
			}
			double[] newVelHeading = step(velocity, heading, targetVelocity, targetHeading);
			velocity = newVelHeading[0];
			heading = newVelHeading[1];
			predicted.project(Vec2.direction(heading), velocity);
			STATES[index][0] = velocity;
			STATES[index][1] = heading;
			POSITIONS[index].setLocation(predicted);
			waveDistance += waveVelocity;
			if (waveDistance >= currentDistance) {
				PATH_BREAK[pathIndex] = step;
				break;
			}
			index++;
			step++;
		}
	}


	private double[] step(double vel, double heading, double targetVel, double targetHeading) {
		return new double[]{
				getNextVelocity(vel, targetVel),
				getNextHeading(vel, heading, targetHeading),
		};
	}

	private static double accelerate(double velocity, double preferredDirection) {
		return InfernoUtils.limit(-ROBOT_MAX_VELOCITY, velocity + (velocity > 0 ? 1 : (velocity < 0 ? -1 : preferredDirection)), ROBOT_MAX_VELOCITY);
	}

	private static double decelerate(double velocity) {
		return Math.abs(velocity) <= 2 ? 0 : velocity + (velocity > 0 ? -2 : (velocity < 0 ? 2 : 0));
	}

	//TODO: Add Deceleration Through Zero
	private static double getNextVelocity(double velocity, double target) {
		double velocitySign = Math.signum(velocity);
		double targetSign = Math.signum(target);
		boolean sameSign = targetSign == velocitySign;
		if (target == 0 || (!sameSign && velocitySign != 0)) {
			return decelerate(velocity);
		}
		double difference = target - velocity;
		boolean possiblyInRange = Math.abs(difference) <= 2;
		double differenceSign = Math.signum(difference);
		if (possiblyInRange) {
			double signedDifference = difference * differenceSign;
			if (signedDifference <= 1 && signedDifference >= -2) {
				return target;
			}
		}
		return accelerate(velocity, differenceSign);
	}

	private static double getNextHeading(double velocity, double heading, double target) {
		double maxTurn = Math.toRadians(10 - 0.75 * Math.abs(velocity));
		return heading + InfernoUtils.limit(-maxTurn, normalRelativeAngle(target - heading), maxTurn);
	}

	//TODO: Add Non-Iterative Wall Smoothing
	private double wallSmooth(Vec2 position, double heading, double orientation) {
		if (wallStick(position, Vec2.direction(heading))) {
			heading += orientation;
			int safetyCounter = 0;
			while (wallStick(position, Vec2.direction(heading)) && safetyCounter < 100) {
				heading += orientation;
				safetyCounter++;
			}
		}
		return heading;
	}

	private boolean wallStick(Vec2 source, Vec2 direction) {
		return !BATTLEFIELD.contains(Vec2.project(source, direction, WALL_STICK_LENGTH));
	}

	public int endIndex(int pathIndex) {
		return PATH_BREAK[pathIndex];
	}

	public Vec2 position(int pathIndex, int index) {
		return POSITIONS[pathIndex * PATH_LENGTH + index];
	}

	public double[] instructions(int path, int step) {
		return STATES[path * PATH_LENGTH + step];
	}
}
