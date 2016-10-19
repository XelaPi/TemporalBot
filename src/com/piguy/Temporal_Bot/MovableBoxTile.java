package com.piguy.Temporal_Bot;

/**
 * Represents the movable box tiles on the board
 *
 * @author Alex Vanyo
 */
public class MovableBoxTile {
    // TODO: Extend tile, and remove from "Movable" categories

    private int x;
    private int y;

    private String color;

    public MovableBoxTile(int x, int y, String color) {
        this.x = x;
        this.y = y;

        this.color = color;
    }

    /**
     * @return the color of the box tile
     */
    public String getColor() {
        return color;
    }

    /**
     * @return the x position of the tile
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y position of the tile
     */
    public int getY() {
        return y;
    }

    private static final String LOG_TAG = "MovableBoxTile";
}
