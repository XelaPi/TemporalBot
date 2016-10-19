package com.piguy.Temporal_Bot;

import java.util.ArrayList;

/**
 * Parent class for all movable objects in the game
 *
 * @author Alex Vanyo
 */
public class Movable {

    // Variables that remember the initial state of the movable (at board creation)
    private int initX;
    private int initY;
    private int initDirection;
    private boolean initVisible;

    // Variables that only matter graphically, and handle the animations and view positions
    private float viewX;
    private float viewY;
    private float viewDirection;
    private float originalViewDirection;
    private long timeStarted;
    private float percentDone;
    private boolean animatingMove;
    private boolean animatingTurn;

    // Variables that are responsible for where an object is and its true position
    private int x;
    private int y;
    private int direction;
    private boolean visible;

    public Movable(int x, int y, int direction, float viewDirection, boolean visible) {
        this.initX = x;
        this.initY = y;
        this.initDirection = direction;
        this.initVisible = visible;

        this.x = x;
        this.y = y;
        this.direction = direction;
        this.visible = visible;

        this.viewX = x;
        this.viewY = y;
        this.viewDirection = viewDirection;
        this.animatingMove = false;
        this.animatingTurn = false;
    }

    /**
     * Attempts to move this movable based on the passed parameters. This function can be recursive, and calls other movables to se if they can move.
     *
     * @param direction           direction that the movable wants to move
     * @param board               tile array denoting the current board
     * @param movables            movable array denoting all of the current movables
     * @param time                time at which the move was initiated for sake of animation
     * @param changeViewDirection should this move change the direction of the movable
     * @return whether or not this move was successful
     */
    public boolean move(int direction, Tile[][] board, ArrayList<Movable> movables, long time, boolean changeViewDirection) {

        this.direction = direction;

        boolean moved = false;

        // Check to see if the direction is back in time
        if (direction == MOVE_BACK_IN_TIME) {
            visible = false;

            moved = true;
        } else {

            this.direction = direction;

            // Determine if the movable can move
            possibleMove:
            if (!board[y + MOVE_ARRAY[direction][1]][x + MOVE_ARRAY[direction][0]].getID().equals(Tile.TILE_WALL)) {
                for (Movable movable : movables) {
                    // Move using the MOVE_ARRAY: a two dimensional array giving whether the x/y coordinate are +/-
                    if (movable.isVisible() && movable.getX() == x + MOVE_ARRAY[direction][0] && movable.getY() == y + MOVE_ARRAY[direction][1]) {
                        moved = movable.move(direction, board, movables, time, false);
                        // Break from testing code as we know whether or not we can move now
                        break possibleMove;
                    }
                }
                moved = true;
            }
        }

        // Update animation details based on whether the movable moved
        animatingTurn = changeViewDirection;
        if (animatingTurn) {
            percentDone = 0;
            timeStarted = time;

            float tempDirection = direction - viewDirection;
            if (tempDirection > 2) {
                viewDirection += 4;
            } else if (tempDirection < -2) {
                viewDirection -= 4;
            }
            originalViewDirection = viewDirection;
        }

        if (moved) {
            viewX = x;
            viewY = y;

            animatingMove = true;
            percentDone = 0;
            timeStarted = time;

            x += MOVE_ARRAY[direction][0];
            y += MOVE_ARRAY[direction][1];

            return true;
        } else {
            viewX = x;
            viewY = y;
            animatingMove = false;

            return false;
        }
    }

    /**
     * Update the movable for the purposes of animating it
     *
     * @param elapsedTime current time on the board
     */
    public void update(long elapsedTime) {
        long timeExisted = elapsedTime - timeStarted;
        percentDone = (float) timeExisted / ANIMATION_LENGTH;

        if (percentDone >= 1) {
            viewX = x;
            viewY = y;
            animatingMove = false;

            viewDirection = direction;
            animatingTurn = false;
        } else {
            if (animatingMove) {
                viewX = x - MOVE_ARRAY[direction][0] * (1 - percentDone);
                viewY = y - MOVE_ARRAY[direction][1] * (1 - percentDone);
            }
            if (animatingTurn) {
                viewDirection = originalViewDirection + (direction - originalViewDirection) * percentDone;
            }
        }
    }

    /**
     * Resets the movable to its position at the beginning of the game
     */
    public void reset() {
        x = initX;
        y = initY;
        direction = initDirection;
        visible = initVisible;

        viewX = initX;
        viewY = initY;
        viewDirection = initDirection;

        animatingMove = false;
        animatingTurn = false;
    }

    /**
     * @return current x position
     */
    public int getX() {
        return x;
    }

    /**
     * @return current view x position
     */
    public float getViewX() {
        return viewX;
    }

    /**
     * @return initial x position
     */
    public int getInitX() {
        return initX;
    }

    /**
     * @return current y position
     */
    public int getY() {
        return y;
    }

    /**
     * @return current view x position
     */
    public float getViewY() {
        return viewY;
    }

    /**
     * @return initial x position
     */
    public int getInitY() {
        return initY;
    }

    /**
     * @return direction
     */
    public int getDirection() {
        return direction;
    }

    /**
     * @return view direction
     */
    public float getViewDirection() {
        return viewDirection;
    }

    /**
     * @return is the movable animating
     */
    public boolean isAnimating() {
        return animatingMove || animatingTurn;
    }

    /**
     * @return is the movable visible
     */
    public boolean isVisible() {
        return visible;
    }

    public static final int MOVE_UP = 0;
    public static final int MOVE_RIGHT = 1;
    public static final int MOVE_DOWN = 2;
    public static final int MOVE_LEFT = 3;
    public static final int MOVE_BACK_IN_TIME = 4;

    private static final int[][] MOVE_ARRAY = new int[5][2];

    static {
        MOVE_ARRAY[MOVE_UP] = new int[]{0, -1};
        MOVE_ARRAY[MOVE_RIGHT] = new int[]{1, 0};
        MOVE_ARRAY[MOVE_DOWN] = new int[]{0, 1};
        MOVE_ARRAY[MOVE_LEFT] = new int[]{-1, 0};
        MOVE_ARRAY[MOVE_BACK_IN_TIME] = new int[]{0, 0};
    }

    private static final long ANIMATION_LENGTH = 100;

    private static final String LOG_TAG = "Movable";
}
