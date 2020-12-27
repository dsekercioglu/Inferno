package dsekercioglu.mega.control;

import dsekercioglu.mega.utility.DoubleCycler;
import dsekercioglu.mega.utility.Vec2Cycler;
import dsekercioglu.mega.math.Vec2;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class InfernoDataManager {

	public final double BATTLE_FIELD_WIDTH;
	public final double BATTLE_FIELD_HEIGHT;

	private final AdvancedRobot ROBOT;

	private double botEnergy;
	private double botGunHeat;
	private double botGunHeading;
	private double botRadarHeading;

	private final Vec2 CURRENT_ROBOT_POSITION = new Vec2(0, 0);
	private final Vec2 CURRENT_ABSOLUTE_DIRECTION = new Vec2(0, 0);
	private final Vec2 CURRENT_ENEMY_POSITION = new Vec2(0, 0);

	private final Vec2Cycler ROBOT_POSITION = new Vec2Cycler(3);
	private final Vec2Cycler ENEMY_POSITION = new Vec2Cycler(3);

	private final DoubleCycler HEADING = new DoubleCycler(3);
	private final DoubleCycler BEARING = new DoubleCycler(3);
	private final DoubleCycler ABSOLUTE_BEARING = new DoubleCycler(3);
	private final DoubleCycler DISTANCE = new DoubleCycler(3);
	private final DoubleCycler VELOCITY = new DoubleCycler(3);
	private final DoubleCycler LATERAL_POTENTIAL = new DoubleCycler(3);
	private final DoubleCycler LATERAL_ACCEL = new DoubleCycler(3);
	private final DoubleCycler ADVANCING_POTENTIAL = new DoubleCycler(3);
	private final DoubleCycler LAT_DIR = new DoubleCycler(3);
	private final DoubleCycler FORWARD_WALL = new DoubleCycler(3);
	private final DoubleCycler BACKWARD_WALL = new DoubleCycler(3);

	private final DoubleCycler ENEMY_ENERGY = new DoubleCycler(3);
	private final DoubleCycler ENEMY_HEADING = new DoubleCycler(3);
	private final DoubleCycler ENEMY_RELATIVE_HEADING = new DoubleCycler(3);
	private final DoubleCycler ENEMY_VELOCITY = new DoubleCycler(3);
	private final DoubleCycler ENEMY_LATERAL_POTENTIAL = new DoubleCycler(3);
	private final DoubleCycler ENEMY_LATERAL_ACCEL = new DoubleCycler(3);
	private final DoubleCycler ENEMY_ADVANCING_POTENTIAL = new DoubleCycler(3);
	private final DoubleCycler ENEMY_LAT_DIR = new DoubleCycler(3);
	private final DoubleCycler ENEMY_FORWARD_WALL = new DoubleCycler(3);
	private final DoubleCycler ENEMY_BACKWARD_WALL = new DoubleCycler(3);

	private int scans = 0;

	public InfernoDataManager(AdvancedRobot robot) {
		ROBOT = robot;
		BATTLE_FIELD_WIDTH = ROBOT.getBattleFieldWidth();
		BATTLE_FIELD_HEIGHT = ROBOT.getBattleFieldHeight();
	}

	public void run() {
		ROBOT_POSITION.clear();
		DISTANCE.clear();
		ABSOLUTE_BEARING.clear();
		HEADING.clear();
		BEARING.clear();
		VELOCITY.clear();
		LATERAL_POTENTIAL.clear();
		ADVANCING_POTENTIAL.clear();
		LAT_DIR.clear();
		LAT_DIR.update(1);

		ENEMY_ADVANCING_POTENTIAL.clear();
		ENEMY_HEADING.clear();
		ENEMY_RELATIVE_HEADING.clear();
		ENEMY_VELOCITY.clear();
		ENEMY_LATERAL_POTENTIAL.clear();
		ENEMY_ADVANCING_POTENTIAL.clear();
		ENEMY_LAT_DIR.clear();
		ENEMY_LAT_DIR.update(1);
	}

	public void onScannedRobot(ScannedRobotEvent e) {
		botEnergy = ROBOT.getEnergy();
		botGunHeat = ROBOT.getGunHeat();
		botGunHeading = ROBOT.getGunHeadingRadians();
		botRadarHeading = ROBOT.getRadarHeadingRadians();
		HEADING.update(ROBOT.getHeadingRadians());
		BEARING.update(e.getBearingRadians());
		double absoluteBearing = HEADING.get(0) + BEARING.get(0);
		ABSOLUTE_BEARING.update(absoluteBearing);
		double distance = e.getDistance();
		DISTANCE.update(distance);
		CURRENT_ROBOT_POSITION.setLocation(new Vec2(ROBOT.getX(), ROBOT.getY()));
		ROBOT_POSITION.update(CURRENT_ROBOT_POSITION.copy());
		CURRENT_ABSOLUTE_DIRECTION.setLocation(Vec2.direction(ABSOLUTE_BEARING.get(0)));
		Vec2 direction = Vec2.direction(e.getBearingRadians());
		double velocity = e.getVelocity();
		VELOCITY.update(velocity);
		LATERAL_POTENTIAL.update(direction.getX());
		LATERAL_ACCEL.update(direction.getX() * velocity - LATERAL_ACCEL.get(1));
		ADVANCING_POTENTIAL.update(direction.getY());
		double latDir = velocity * direction.getX();
		latDir = latDir == 0 ? LAT_DIR.get(0) : Math.signum(latDir);
		LAT_DIR.update(latDir);

		FORWARD_WALL.update(wallMEA(CURRENT_ENEMY_POSITION, distance, absoluteBearing + Math.PI, latDir));
		BACKWARD_WALL.update(wallMEA(CURRENT_ENEMY_POSITION, distance, absoluteBearing + Math.PI, -latDir));

		ENEMY_ENERGY.update(e.getEnergy());
		CURRENT_ENEMY_POSITION.setLocation(Vec2.project(CURRENT_ROBOT_POSITION, CURRENT_ABSOLUTE_DIRECTION, DISTANCE.get(0)));
		ENEMY_POSITION.update(CURRENT_ENEMY_POSITION.copy());
		ENEMY_HEADING.update(e.getHeadingRadians());
		ENEMY_RELATIVE_HEADING.update(ABSOLUTE_BEARING.get(0) - ENEMY_HEADING.get(0) + Math.PI);
		Vec2 enemyDirection = Vec2.direction(ENEMY_RELATIVE_HEADING.get(0));
		double enemyVelocity = e.getVelocity();
		ENEMY_VELOCITY.update(enemyVelocity);
		ENEMY_LATERAL_ACCEL.update(enemyDirection.getX() * enemyVelocity - ENEMY_LATERAL_ACCEL.get(1));
		ENEMY_LATERAL_POTENTIAL.update(enemyDirection.getX());
		ENEMY_ADVANCING_POTENTIAL.update(enemyDirection.getY());
		double enemyLatDir = enemyVelocity * enemyDirection.getX();
		ENEMY_LAT_DIR.update(enemyLatDir == 0 ? ENEMY_LAT_DIR.get(0) : Math.signum(enemyLatDir));

		ENEMY_FORWARD_WALL.update(wallMEA(CURRENT_ROBOT_POSITION, distance, absoluteBearing, enemyLatDir));
		ENEMY_BACKWARD_WALL.update(wallMEA(CURRENT_ROBOT_POSITION, distance, absoluteBearing, -enemyLatDir));

		scans++;
	}

	private double wallMEA(Vec2 orbit, double distance, double absBearing, double dir) {
		double max = 0;
		double min = -Math.PI * dir;
		double pred = -Math.PI / 2 * dir;
		for (int i = 0; i < 7; i++) {
			if (inBounds(Vec2.project(orbit, Vec2.direction(absBearing + pred), distance))) {
				max = pred;
				pred = (pred + min) * 0.5;
			} else {
				min = pred;
				max = (max + pred) * 0.5;
			}
		}
		return pred * -dir;
	}

	public boolean inBounds(Vec2 position) {
		return position.getX() > 18 && position.getY() > 18 && position.getX() < BATTLE_FIELD_WIDTH - 18 && position.getY() < BATTLE_FIELD_HEIGHT;
	}

	public double getBotEnergy() {
		return botEnergy;
	}

	public double getBotGunHeat() {
		return botGunHeat;
	}

	public double getBotGunHeading() {
		return botGunHeading;
	}

	public double getBotRadarHeading() {
		return botRadarHeading;
	}

	public Vec2 getCurrentRobotPosition() {
		return CURRENT_ROBOT_POSITION;
	}

	public Vec2 getCurrentEnemyPosition() {
		return CURRENT_ENEMY_POSITION;
	}

	public Vec2 getCurrentAbsoluteDirection() {
		return CURRENT_ABSOLUTE_DIRECTION;
	}

	public Vec2 getRobotPosition(int past) {
		return ROBOT_POSITION.get(past);
	}

	public double getVelocity(int past) {
		return VELOCITY.get(past);
	}

	public double getLateralAccel(int past) {
		return LATERAL_ACCEL.get(past);
	}

	public double getLateralPotential(int past) {
		return LATERAL_POTENTIAL.get(past);
	}

	public double getAdvancingPotential(int past) {
		return ADVANCING_POTENTIAL.get(past);
	}

	public double getLateralDirection(int past) {
		return LAT_DIR.get(past);
	}

	public Vec2 getEnemyPosition(int past) {
		return ENEMY_POSITION.get(past);
	}

	public double getEnemyEnergy(int past) {
		return ENEMY_ENERGY.get(past);
	}

	public double getDistance(int past) {
		return DISTANCE.get(past);
	}

	public double getHeading(int past) {
		return HEADING.get(past);
	}

	public double getBearing(int past) {
		return BEARING.get(past);
	}

	public double getEnemyHeading(int past) {
		return ENEMY_HEADING.get(past);
	}

	public double getEnemyVelocity(int past) {
		return ENEMY_VELOCITY.get(past);
	}

	public double getEnemyLateralAccel(int past) {
		return ENEMY_LATERAL_ACCEL.get(past);
	}

	public double getEnemyLateralPotential(int past) {
		return ENEMY_LATERAL_POTENTIAL.get(past);
	}

	public double getEnemyAdvancingPotential(int past) {
		return ENEMY_ADVANCING_POTENTIAL.get(past);
	}

	public double getEnemyLateralDirection(int past) {
		return ENEMY_LAT_DIR.get(past);
	}

	public double getAbsoluteBearing(int past) {
		return ABSOLUTE_BEARING.get(past);
	}

	public double getForwardWallDistance(int past) {
		return FORWARD_WALL.get(past);
	}

	public double getBackwardWallDistance(int past) {
		return BACKWARD_WALL.get(past);
	}

	public double getEnemyForwardWallDistance(int past) {
		return ENEMY_FORWARD_WALL.get(past);
	}

	public double getEnemyBackwardWallDistance(int past) {
		return ENEMY_BACKWARD_WALL.get(past);
	}

	public int getScans() {
		return scans;
	}

}
