package com.piguy.Temporal_Bot;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Definition and wrapper for a score
 *
 * @author Alex Vanyo
 */
public class Score implements Parcelable {

    private int id;
    private boolean completed;
    private long time;
    private int robots;

    public Score(int id, boolean completed, long time, int robots) {
        this.id = id;
        this.completed = completed;
        this.time = time;
        this.robots = robots;
    }

    public Score(int id) {
        this.id = id;
        this.completed = DEFAULT_SCORE.getCompleted();
        this.time = DEFAULT_SCORE.getTime();
        this.robots = DEFAULT_SCORE.getRobots();
    }

    private Score(Parcel in) {
        id = in.readInt();
        completed = in.readInt() == 1;
        time = in.readLong();
        robots = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeInt(completed ? 1 : 0);
        out.writeLong(time);
        out.writeInt(robots);
    }

    public static final Parcelable.Creator<Score> CREATOR = new Parcelable.Creator<Score>() {
        public Score createFromParcel(Parcel in) {
            return new Score(in);
        }

        public Score[] newArray(int size) {
            return new Score[size];
        }
    };

    /**
     * @return score id
     */
    public int getID() {
        return id;
    }

    /**
     * @return if the level has been completed
     */
    public boolean getCompleted() {
        return completed;
    }

    /**
     * @return the fastest time in which the level has been completed
     */
    public long getTime() {
        return time;
    }

    /**
     * Static method to turn a string from milliseconds into a readable form
     *
     * @param time time to beautify
     * @return time in minutes, seconds, and milliseconds
     */
    public static String getTimeMinSec(long time) {
        int minutes = (int) Math.floor(time / 1000 / 60);
        int seconds = (int) Math.floor((time - (minutes * 1000 * 60)) / 1000);
        int milliseconds = (int) time - (minutes * 1000 * 60) - (seconds * 1000);

        String stringMinutes = "" + minutes;
        String stringSeconds = "" + ((seconds < 10 && minutes > 0) ? "0" + seconds : seconds);
        String stringMilliseconds = "" + (milliseconds >= 100 ? milliseconds : "0" + (milliseconds >= 10 ? milliseconds : "0" + milliseconds));

        return (minutes > 0 ? stringMinutes + ":" : "") + (minutes > 0 || seconds > 0 ? stringSeconds : "0") + "." + stringMilliseconds;
    }

    /**
     * @return the least number of robots in which the level has been completed
     */
    public int getRobots() {
        return robots;
    }

    public static final Score DEFAULT_SCORE = new Score(0, false, 999995, 99);
}
