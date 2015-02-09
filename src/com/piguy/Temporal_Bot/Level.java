package com.piguy.Temporal_Bot;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Definition and wrapper for a level
 *
 * @author Alex Vanyo
 */
public class Level implements Parcelable {

	private int id;
	private String levelString;
	private String name;
	private long targetTime;
	private int targetRobots;
	private String instruction;
	private Score score;

	public Level(int id, String levelString, String name, long targetTime, int targetRobots, String instruction, Score score) {
		this.id = id;
		this.levelString = levelString;
		this.name = name;
		this.targetTime = targetTime;
		this.targetRobots = targetRobots;
		this.instruction = instruction;
		this.score = score;
	}

	private Level(Parcel in) {
		id = in.readInt();
		levelString = in.readString();
		name = in.readString();
		targetTime = in.readLong();
		targetRobots = in.readInt();
		instruction = in.readString();
		score = in.readParcelable(Score.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(levelString);
		out.writeString(name);
		out.writeLong(targetTime);
		out.writeInt(targetRobots);
		out.writeString(instruction);
		out.writeParcelable(score, flags);
	}

	public static final Parcelable.Creator<Level> CREATOR = new Parcelable.Creator<Level>() {
		public Level createFromParcel(Parcel in) {
			return new Level(in);
		}

		public Level[] newArray(int size) {
			return new Level[size];
		}
	};

	/**
	 * Refresh the score for this level
	 *
	 * @param newScore
	 */
	public void updateScore(Score newScore) {
		this.score = new Score(this.id, true, newScore.getTime() <= this.score.getTime() ? newScore.getTime() : this.score.getTime(), newScore.getRobots() <= this.score.getRobots() ? newScore.getRobots() : this.score.getRobots());
	}

	/**
	 * @return the id for this level
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return the level string for this level
	 */
	public String getLevelString() {
		return levelString;
	}

	/**
	 * @return the name of this level
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the target time for this level
	 */
	public long getTargetTime() {
		return targetTime;
	}

	/**
	 * @return the target number of robots for this level
	 */
	public int getTargetRobots() {
		return targetRobots;
	}

	/**
	 * @return the score for this level
	 */
	public Score getScore() {
		return score;
	}

	/**
	 * @return if this level has instruction information
	 */
	public boolean hasInstruction() {
		return instruction != null;
	}

	/**
	 * @return the instructions for this level
	 */
	public String getInstruction() {
		return instruction;
	}

	public static final Level DEFAULT_LEVEL = new Level(0, "111_1*1_111_", "Default Level", 999990, 1, null, new Score(0));
}
