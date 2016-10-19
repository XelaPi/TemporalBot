package com.piguy.Temporal_Bot;

/**
 * Represents the movable boxes on the board
 *
 * @author Alex
 */
public class MovableBox extends Movable {

    private String color;

    public MovableBox(int x, int y, int direction, String color) {
        super(x, y, direction, direction, true);

        this.color = color;
    }

    /**
     * @return the color of the box
     */
    public String getColor() {
        return color;
    }

    // Constants for each type of movable box
    public static enum MOVABLE_BOX_COLORS {
        YELLOW("YELLOW");

        private String id;

        MOVABLE_BOX_COLORS(String id) {
            this.id = id;
        }

        public String getID() {
            return id;
        }
    }

    private static final String LOG_TAG = "MovableBox";
}
