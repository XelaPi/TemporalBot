package com.piguy.Temporal_Bot;

/**
 * Each instance of this class denotes a move command for the robot
 *
 * @author Alex Vanyo
 */
public class MoveCommand {

	private int direction;
	private long executeTime;
	private boolean executed;

	public MoveCommand(int direction, long executeTime) {
		this.direction = direction;
		this.executeTime = executeTime;

		executed = false;
	}

	/**
	 * Reset the command so that it can be executed again
	 */
	public void reset() {
		executed = false;
	}

	/**
	 * Determines if this command is ready to be executed
	 *
	 * @param elapsedTime current time on the board
	 * @return should this move command be executed
	 */
	public boolean execute(long elapsedTime) {
		if (executeTime <= elapsedTime && !executed) {
			executed = true;
			return true;
		}

		return false;
	}

	/**
	 * @return direction for the move command
	 */
	public int getDirection() {
		return direction;
	}

	/**
	 * @return at what time should this move be executed
	 */
	public long getExecuteTime() {
		return executeTime;
	}

	private static final String LOG_TAG = "MoveCommand";
}
