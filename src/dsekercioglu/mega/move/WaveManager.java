package dsekercioglu.mega.move;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.InfernoUtils;
import dsekercioglu.mega.control.InfernoProfiler;
import dsekercioglu.mega.control.InfernoTaskManager;
import dsekercioglu.mega.treelearn.ProfileN;
import dsekercioglu.mega.treelearn.Node;
import dsekercioglu.mega.treelearn.Normalize;
import dsekercioglu.mega.treelearn.WeightedCombine;
import dsekercioglu.mega.utility.DoubleList;
import dsekercioglu.mega.utility.Vec2List;
import dsekercioglu.mega.math.Vec2;
import robocode.Bullet;
import robocode.HitByBulletEvent;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WaveManager {

	private static final int DEBUG_BINS = 31;
	private static final int DEBUG_MID_BIN = DEBUG_BINS / 2;

	private final InfernoDataManager DATA_MANAGER;
	private final InfernoTaskManager TASK_MANAGER;
	private final InfernoProfiler PROFILER;

	private final Vec2List SOURCE = new Vec2List(10);
	private final List<double[]> ATT = new ArrayList<>();//Lat Vel, Adv Vel, Distance, Forward Wall, Backward Wall
	private final List<double[]> DIR_ABS_BEARING_MEA = new ArrayList<>(10);
	private final List<double[]> VEL_DIST_TIME = new ArrayList<>(10);
	private final List<WaveDanger> DANGERS = new ArrayList<>();

	private final Node LEARN_SYSTEM;

	public WaveManager(InfernoDataManager dataManager, InfernoTaskManager taskManager, InfernoProfiler profiler) {
		DATA_MANAGER = dataManager;
		TASK_MANAGER = taskManager;
		PROFILER = profiler;
		LEARN_SYSTEM = new WeightedCombine(
				new double[]{1, 1},
				new Normalize(
						new ProfileN(
								new int[]{0, 1, 2},
								new double[]{8, 2, 4},
								30
						)
				),
				new Normalize(
						new ProfileN(
								new int[]{0, 1, 2, 3, 4, 5},
								new double[]{8, 2, 4, 6, 4, 3},
								30
						)
				)
		);
	}

	public void update() {
		updateWaves();
		double deltaEnergy = DATA_MANAGER.getEnemyEnergy(1) - DATA_MANAGER.getEnemyEnergy(0);
		if (deltaEnergy > 0.99 && deltaEnergy < 3.01) {
			PROFILER.push("Enemy Fire");
			double bulletVelocity = Rules.getBulletSpeed(deltaEnergy);
			SOURCE.push(DATA_MANAGER.getEnemyPosition(1).copy());
			DIR_ABS_BEARING_MEA.add(
					new double[]{DATA_MANAGER.getLateralDirection(1),
							DATA_MANAGER.getAbsoluteBearing(1) + Math.PI,
							InfernoUtils.mea(bulletVelocity)}
			);
			VEL_DIST_TIME.add(new double[]{bulletVelocity, bulletVelocity * 2, 2});

			double velocity = DATA_MANAGER.getVelocity(2);
			double[] dataPoint = {
					Math.abs(DATA_MANAGER.getLateralPotential(2) * velocity) / 8,
					DATA_MANAGER.getAdvancingPotential(2) / 16 + 0.5,
					DATA_MANAGER.getDistance(2) / 1000,
					(DATA_MANAGER.getLateralAccel(2) + 2) / 3,
					DATA_MANAGER.getForwardWallDistance(2) / Math.PI,
					DATA_MANAGER.getBackwardWallDistance(2) / Math.PI,
			};
			ATT.add(dataPoint);
			WaveDanger waveDanger = predict(dataPoint);
			DANGERS.add(waveDanger);
			TASK_MANAGER.logTask(InfernoTaskManager.MOVE_PREDICT);
			PROFILER.pop();
		}
	}

	public double getDanger(Vec2 position) {
		double danger = 0;
		for (int i = 0; i < SOURCE.size(); i++) {
			double angle = SOURCE.get(i).angleTo(position);
			danger += DANGERS.get(i).getDanger(guessFactor(angle, i)) * VEL_DIST_TIME.get(i)[1];
		}
		return danger;
	}

	public double getDanger(double guessFactor, int index) {
		double danger = 0;
		danger += DANGERS.get(index).getDanger(guessFactor) * VEL_DIST_TIME.get(index)[1];
		return danger;
	}

	private void updateWaves() {
		for (int i = 0; i < SOURCE.size(); i++) {
			double[] velDistTime = VEL_DIST_TIME.get(i);
			velDistTime[1] += velDistTime[0];
			velDistTime[2]++;
			if (InfernoUtils.sq(velDistTime[1]) > SOURCE.get(i).distanceSq(DATA_MANAGER.getCurrentRobotPosition())) {
				removeWave(i);
				i--;
			}
		}
	}

	private void removeWave(int index) {
		ATT.remove(index);
		SOURCE.remove(index);
		DIR_ABS_BEARING_MEA.remove(index);
		VEL_DIST_TIME.remove(index);
		DANGERS.remove(index);
	}

	public void onHitByBullet(HitByBulletEvent e) {
		Bullet bullet = e.getBullet();
		logHit(bullet);
	}

	private void logHit(Bullet bullet) {
		Vec2 bulletPos = new Vec2(bullet.getX(), bullet.getY());
		for (int i = 0; i < VEL_DIST_TIME.size(); i++) {
			double[] velDistTime = VEL_DIST_TIME.get(i);
			double expectedDistanceTraveled = velDistTime[2] * bullet.getVelocity();
			if (Math.abs(velDistTime[0] - bullet.getVelocity()) < 0.2) {
				if (SOURCE.get(i).distance(bulletPos) - expectedDistanceTraveled < velDistTime[0] * 2) {
					double angle = SOURCE.get(i).angleTo(bulletPos);
					LEARN_SYSTEM.addData(
							ATT.get(i),
							new double[]{guessFactor(angle, i), 1}
					);
					removeWave(i);
					return;
				}
			}
		}
	}

	public double guessFactor(double angle, int index) {
		double[] dirAbsBearingMea = DIR_ABS_BEARING_MEA.get(index);
		return Utils.normalRelativeAngle(angle - dirAbsBearingMea[1]) / dirAbsBearingMea[2] * dirAbsBearingMea[0];
	}

	public double angle(double guessFactor, int index) {
		double[] dirAbsBearingMea = DIR_ABS_BEARING_MEA.get(index);
		return dirAbsBearingMea[1] + guessFactor * dirAbsBearingMea[2] * dirAbsBearingMea[0];
	}

	private WaveDanger predict(double[] dataPoint) {
		List<double[]> guessFactorsWeights = LEARN_SYSTEM.predict(dataPoint);
		return new WaveDanger(guessFactorsWeights);
	}

	public void onPaint(Graphics2D g) {
		for (int i = 0; i < SOURCE.size(); i++) {
			double[] dirAbsBearingMea = DIR_ABS_BEARING_MEA.get(i);
			WaveDanger danger = DANGERS.get(i);
			Vec2 source = SOURCE.get(i);
			int distanceTraveled = (int) (VEL_DIST_TIME.get(i)[1] + 0.5);
			g.drawOval((int) (source.getX() - distanceTraveled + 0.5), (int) (source.getY() - distanceTraveled + 0.5), distanceTraveled * 2, distanceTraveled * 2);

			DoubleList dangers = new DoubleList(DEBUG_BINS);
			Vec2List points = new Vec2List(DEBUG_BINS);
			for (int j = 0; j < DEBUG_BINS; j++) {
				double gf = (j - DEBUG_MID_BIN) * 1D / DEBUG_MID_BIN;
				double angle = dirAbsBearingMea[1] + gf * dirAbsBearingMea[2] * dirAbsBearingMea[0];
				dangers.push(danger.getDanger(gf));
				Vec2 point = Vec2.project(SOURCE.get(i), Vec2.direction(angle), VEL_DIST_TIME.get(i)[1]);
				points.push(point);
			}

			double mean = InfernoUtils.mean(dangers);
			double stdDev = Math.sqrt(InfernoUtils.variance(dangers, mean));
			double min = mean - 2 * stdDev;
			double diff = 4 * stdDev;
			for (int j = 0; j < DEBUG_BINS; j++) {
				Vec2 point = points.get(j);
				double value = InfernoUtils.limit((dangers.get(j) - min) / diff, 0, 1);
				g.setColor(colorMorph(value));
				g.fillOval((int) (point.getX() - 2.5), (int) (point.getY() - 2.5), 6, 6);
			}

			/*
			double[] safePoints = danger.safeGuessFactors(-1, 1, 0.3, 15, 5);
			for (double safePoint : safePoints) {
				double angle = dirAbsBearingMea[1] + safePoint * dirAbsBearingMea[2] * dirAbsBearingMea[0];
				Vec2 point = Vec2.project(SOURCE.get(i), Vec2.direction(angle), VEL_DIST_TIME.get(i)[1]);
				g.setColor(Color.GREEN);
				g.fillOval((int) (point.getX() - 2.5), (int) (point.getY() - 2.5), 6, 6);
			}
			 */
		}
	}

	private Color colorMorph(double value) {
		return new Color((int) (value * 255), 0, (int) ((1 - value) * 255));
	}

	public Vec2List getSource() {
		return SOURCE;
	}

	public List<double[]> getDirAbsBearingMEA() {
		return DIR_ABS_BEARING_MEA;
	}

	public List<double[]> getVelDistTime() {
		return VEL_DIST_TIME;
	}

	public List<WaveDanger> getWaveDanger() {
		return DANGERS;
	}
}