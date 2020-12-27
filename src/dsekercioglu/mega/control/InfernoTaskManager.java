package dsekercioglu.mega.control;

//TODO: Measure time and use actual values
public class InfernoTaskManager {

	public static final int TARGET_PREDICT = 7500;
	public static final int MOVE_PREDICT = 5000;
	public static final int GENERATE_PATH = 7500;

	private static final int TASK_QUOTA = 100000;
	private int nextTaskQuota = TASK_QUOTA;
	private int nextAllocatedTaskQuota;

	private int currentTaskQuota;
	private int allocatedTaskQuota;

	public void update() {
		nextTaskQuota = TASK_QUOTA;
		nextAllocatedTaskQuota = 0;
	}


	public void allocateTask(int taskCost) {
		nextTaskQuota -= taskCost;
		nextAllocatedTaskQuota += taskCost;
	}

	public void logTask(int taskCost) {
		currentTaskQuota -= taskCost;
	}

	public void logAllocatedTask(int taskCost) {
		allocatedTaskQuota -= taskCost;
	}

	public boolean allowTask(int taskCost) {
		return currentTaskQuota >= taskCost;
	}

	public boolean allowAllocatedTask(int taskCost) {
		return allocatedTaskQuota >= taskCost;
	}

	public void lateUpdate() {
		if (nextAllocatedTaskQuota > TASK_QUOTA) {
			nextAllocatedTaskQuota = TASK_QUOTA;
			nextTaskQuota = 0;
		}
		currentTaskQuota = nextTaskQuota;
		allocatedTaskQuota = nextAllocatedTaskQuota;
	}

	public void debug() {
		System.out.println("Allocated: " + nextAllocatedTaskQuota);
		System.out.println("Normal: " + nextTaskQuota);
	}
}
