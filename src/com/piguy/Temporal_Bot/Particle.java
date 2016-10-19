package com.piguy.Temporal_Bot;

/**
 * Represents any particle effect on the board
 *
 * @author Alex Vanyo
 */
public class Particle {

    private int x;
    private int y;
    private float percentDone;
    private long timeStarted;

    public Particle(int x, int y, long timeStarted) {
        this.x = x;
        this.y = y;

        this.percentDone = 0;
        this.timeStarted = timeStarted;
    }

    /**
     * @param elapsedTime current time on the board
     * @return if this animation is complete
     */
    public boolean update(long elapsedTime) {
        long timeExisted = elapsedTime - timeStarted;
        percentDone = (float) timeExisted / ANIMATION_LENGTH;

        return percentDone >= 1.0;
    }

    /**
     * @return x position of the particle
     */
    public int getX() {
        return x;
    }

    /**
     * @return y position of the particle
     */
    public int getY() {
        return y;
    }

    /**
     * @return percent that the animation is complete
     */
    public double getPercentDone() {
        return percentDone;
    }

    private static final long ANIMATION_LENGTH = 500;

    private static final String LOG_TAG = "Particle";
}
