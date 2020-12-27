package dsekercioglu.mega.gun;

import dsekercioglu.mega.control.InfernoDataManager;
import dsekercioglu.mega.InfernoUtils;
import dsekercioglu.mega.treelearn.ProfileN;
import dsekercioglu.mega.treelearn.Node;
import dsekercioglu.mega.treelearn.Normalize;
import dsekercioglu.mega.treelearn.WeightedCombine;
import dsekercioglu.mega.utility.Vec2List;
import dsekercioglu.mega.math.Vec2;
import robocode.Rules;
import robocode.util.Utils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WaveTracker {

	private static final int K = 50;
	private final InfernoDataManager DATA_MANAGER;

	private final Vec2List SOURCE = new Vec2List(40);
	private final List<double[]> ATT = new ArrayList<>(40);
	private final List<double[]> DIR_ABS_BEARING_MEA = new ArrayList<>(40);
	private final List<double[]> VEL_DIST = new ArrayList<>(40);

	private final Node LEARN_SYSTEM;

	public WaveTracker(InfernoDataManager dataManager) {
		DATA_MANAGER = dataManager;
		LEARN_SYSTEM = new WeightedCombine(
				new double[]{1},
				new Normalize(
						new ProfileN(
								new int[]{0, 1, 2, 3, 4},
								new double[]{10, 2, 4, 4, 3},
								K
						)
				)
		);
	}

	public void run() {
		ATT.clear();
		SOURCE.clear();
		DIR_ABS_BEARING_MEA.clear();
		VEL_DIST.clear();
	}

	public void addWave(double bulletPower) {
		double bulletVelocity = Rules.getBulletSpeed(bulletPower);

		SOURCE.push(DATA_MANAGER.getCurrentRobotPosition().copy());
		DIR_ABS_BEARING_MEA.add(new double[]{DATA_MANAGER.getEnemyLateralDirection(0), DATA_MANAGER.getAbsoluteBearing(0), InfernoUtils.mea(bulletVelocity)});
		VEL_DIST.add(new double[]{bulletVelocity, bulletVelocity});

		ATT.add(getDataPoint(bulletPower));
	}

	public void update() {
		for (int i = 0; i < VEL_DIST.size(); i++) {
			double[] velDist = VEL_DIST.get(i);
			velDist[1] += velDist[0];
			if (SOURCE.get(i).distanceSq(DATA_MANAGER.getCurrentEnemyPosition()) < velDist[1] * velDist[1]) {
				double[] dirAbsBearingMea = DIR_ABS_BEARING_MEA.get(i);
				double angle = Math.atan2(DATA_MANAGER.getCurrentEnemyPosition().getX() - SOURCE.get(i).getX(), DATA_MANAGER.getCurrentEnemyPosition().getY() - SOURCE.get(i).getY());
				double gf = Utils.normalRelativeAngle(angle - dirAbsBearingMea[1]) / dirAbsBearingMea[2] * dirAbsBearingMea[0];
				LEARN_SYSTEM.addData(
						ATT.get(i),
						new double[]{gf, DATA_MANAGER.getScans()}
				);
				ATT.remove(i);
				SOURCE.remove(i);
				DIR_ABS_BEARING_MEA.remove(i);
				VEL_DIST.remove(i);
				i--;
			}
		}
	}

	private double[] getDataPoint(double bulletPower) {
		double enemyVelocity = DATA_MANAGER.getEnemyVelocity(0);
		double latEnemyVelocity = DATA_MANAGER.getEnemyLateralPotential(0) * enemyVelocity;

		return new double[]{Math.abs(latEnemyVelocity) / 8,
				DATA_MANAGER.getEnemyAdvancingPotential(0) * enemyVelocity / 16 + 0.5,
				DATA_MANAGER.getDistance(0) / (Rules.getBulletSpeed(bulletPower) * 91),
				DATA_MANAGER.getEnemyForwardWallDistance(0) / Math.PI,
				DATA_MANAGER.getEnemyBackwardWallDistance(0) / Math.PI};
	}

	public List<double[]> predict(double bulletPower) {
		return LEARN_SYSTEM.predict(getDataPoint(bulletPower));
	}


	public void onPaint(Graphics2D g) {
		for (int i = 0; i < SOURCE.size(); i++) {
			Vec2 source = SOURCE.get(i);
			int distanceTraveled = (int) (VEL_DIST.get(i)[1] + 0.5);
			g.drawOval((int) (source.getX() - distanceTraveled + 0.5), (int) (source.getY() - distanceTraveled + 0.5), distanceTraveled * 2, distanceTraveled * 2);
		}
	}
}
