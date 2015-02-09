package com.piguy.Temporal_Bot;

/**
 * Represents each tile on the board
 *
 * @author Alex Vanyo
 */
public class Tile {

	private String id;
	private String displayID;

	public Tile(String id) {
		this.id = id;
		this.displayID = id;
	}

	/**
	 * @param displayID custom display id to be set for each tile
	 */
	public void setDisplay(String displayID) {
		this.displayID = displayID;
	}

	/**
	 * @return id for the category of the tile
	 */
	public String getID() {
		return id;
	}

	/**
	 * @return display id for the tile
	 */
	public String getDisplayID() {
		return displayID;
	}

	// Constant tags for referencing tiles and tile types
	public static final String TILE_WALL = "TILE_WALL_";
	public static final String TILE_FLOOR = "TILE_FLOOR_";
	public static final String TILE_MOVABLE_BOX = "TILE_MOVABLE_BOX_";

	public static final String ALONE_LABEL = "ALONE_LABEL";
	public static final String ENDPOINT_LABEL = "ENDPOINT_LABEL_";
	public static final String MIDDLE_LABEL = "MIDDLE_LABEL_";
	public static final String CORNER_LABEL = "CORNER_LABEL_";
	public static final String T_LABEL = "T_LABEL_";
	public static final String CROSS_LABEL = "CROSS_LABEL";

	public static final String UP = "UP";
	public static final String RIGHT = "RIGHT";
	public static final String DOWN = "DOWN";
	public static final String LEFT = "LEFT";
	public static final String UP_DOWN = "UP_DOWN";
	public static final String LEFT_RIGHT = "LEFT_RIGHT";

	private static final String LOG_TAG = "Tile";
}
