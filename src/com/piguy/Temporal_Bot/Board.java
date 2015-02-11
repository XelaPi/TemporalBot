package com.piguy.Temporal_Bot;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.*;
import android.graphics.drawable.NinePatchDrawable;
import android.util.Log;

import java.util.*;

/**
 * Defines and draws the board, which handles the game logic
 *
 * @author Alex Vanyo
 */
public class Board extends ContextWrapper {

	private GameStateListener gameStateListener;

	private long elapsedTime;
	private Timer incrementTimer;
	private boolean running;

	private Tile[][] tiles;
	private ArrayList<Robot> robots;
	private ArrayList<MovableBox> movableBoxes;
	private ArrayList<MovableBoxTile> movableBoxTiles;
	private ArrayList<Particle> particles;

	private boolean canWarpBackInTime;
	private boolean won;
	private boolean snapshot;

	private Matrix rotateMatrix;
	private Map<String, Bitmap> bitmapArray;
	private Bitmap bitmapBoardFloor;
	private Bitmap bitmapBoardWalls;
	private NinePatchDrawable bitmapLevelTimeline;

	private Level level;
	private int tileNumberX;
	private int tileNumberY;

	private int drawTileSize;
	private Rect boundsBoard;
	private Rect boundsLevelTimeline;

	public Board(Context context) {
		super(context);

		// Initialize arrays for the board
		robots = new ArrayList<Robot>();
		movableBoxes = new ArrayList<MovableBox>();
		movableBoxTiles = new ArrayList<MovableBoxTile>();
		particles = new ArrayList<Particle>();

		rotateMatrix = new Matrix();
	}

	public synchronized void setGameStateListener(GameStateListener gameStateListener) {
		this.gameStateListener = gameStateListener;
	}

	/**
	 * Sets the board for a certain level, by parsing the passed level string. Also resets the non-wall/floor objects.
	 *
	 * @param level level string as given by Level.getLevelString()
	 * @see Board#reset
	 */
	public synchronized void setLevel(Level level, boolean snapshot) {
		this.level = level;
		this.snapshot = snapshot;
		this.tileNumberX = getTileNumberX(level.getLevelString());
		this.tileNumberY = getTileNumberY(level.getLevelString());

		// Initializes the tile array
		tiles = new Tile[tileNumberY][tileNumberX];

		// Initializes the board to be either floor or wall
		for (int i = 0; i < tileNumberY; i++) {
			for (int j = 0; j < tileNumberX; j++) {
				char tile = level.getLevelString().charAt((i * (tileNumberX + 1)) + j);
				if (Character.isUpperCase(tile)) {
					tiles[i][j] = new Tile(Tile.TILE_MOVABLE_BOX);
				} else {
					tiles[i][j] = new Tile((tile == WALL_CODE) ? Tile.TILE_WALL : Tile.TILE_FLOOR);
				}
			}
		}

		// Gives each tile the direction-based id for drawing purposes
		for (int i = 0; i < tileNumberY; i++) {
			for (int j = 0; j < tileNumberX; j++) {
				if (tiles[i][j].getID().equals(Tile.TILE_MOVABLE_BOX)) {

					tiles[i][j].setDisplay(Tile.TILE_FLOOR + Tile.ALONE_LABEL);

				} else if (tiles[i][j].getID().equals(Tile.TILE_FLOOR) || tiles[i][j].getID().equals(Tile.TILE_WALL)) {
					boolean sameUp = false;
					if (i > 0) {
						sameUp = tiles[i - 1][j].getID().equals(tiles[i][j].getID());
					}

					boolean sameRight = false;
					if (j < tileNumberX - 1) {
						sameRight = tiles[i][j + 1].getID().equals(tiles[i][j].getID());
					}

					boolean sameDown = false;
					if (i < tileNumberY - 1) {
						sameDown = tiles[i + 1][j].getID().equals(tiles[i][j].getID());
					}

					boolean sameLeft = false;
					if (j > 0) {
						sameLeft = tiles[i][j - 1].getID().equals(tiles[i][j].getID());
					}

					// Each number in the switch represents the number of similar tiles around the current tile
					switch ((sameUp ? 1 : 0) + (sameRight ? 1 : 0) + (sameDown ? 1 : 0) + (sameLeft ? 1 : 0)) {
						// No surrounding like tiles
						case 0:
							tiles[i][j].setDisplay(tiles[i][j].getID() + Tile.ALONE_LABEL);
							break;

						// One surrounding like tile
						case 1:
							tiles[i][j].setDisplay(tiles[i][j].getID() + Tile.ENDPOINT_LABEL + (sameUp ? Tile.UP : "") + (sameRight ? Tile.RIGHT : "") + (sameDown ? Tile.DOWN : "") + (sameLeft ? Tile.LEFT : ""));
							break;

						// Two surrounding like tiles. These could be either on opposite sides, in both directions, or in an L-shape
						case 2:
							if (sameUp && sameDown && !sameRight && !sameLeft) {
								tiles[i][j].setDisplay(tiles[i][j].getID() + Tile.MIDDLE_LABEL + Tile.UP_DOWN);
							} else if (!sameUp && !sameDown && sameRight && sameLeft) {
								tiles[i][j].setDisplay(tiles[i][j].getID() + Tile.MIDDLE_LABEL + Tile.LEFT_RIGHT);
							} else {
								tiles[i][j].setDisplay(tiles[i][j].getID() + Tile.CORNER_LABEL + (sameUp && sameRight ? Tile.UP : "") + (sameRight && sameDown ? Tile.RIGHT : "") + (sameDown && sameLeft ? Tile.DOWN : "") + (sameLeft && sameUp ? Tile.LEFT : ""));
							}
							break;

						// Three surrounding like tiles. They form a T-shape
						case 3:
							tiles[i][j].setDisplay(tiles[i][j].getID() + Tile.T_LABEL + (!sameUp ? Tile.UP : "") + (!sameRight ? Tile.RIGHT : "") + (!sameDown ? Tile.DOWN : "") + (!sameLeft ? Tile.LEFT : ""));
							break;

						// Four surrounding like tiles
						case 4:
							tiles[i][j].setDisplay(tiles[i][j].getID() + Tile.CROSS_LABEL);
							break;
					}
				}
			}
		}

		reset();
	}

	/**
	 * Resets the board to its default state. Only touches the parts of the board that can move (robots, etc.)
	 */
	public synchronized void reset() {

		// Clears the arrays
		robots.clear();
		movableBoxes.clear();
		movableBoxTiles.clear();
		particles.clear();

		// Parses the level to initialize each special object
		for (int i = 0; i < tileNumberY; i++) {
			for (int j = 0; j < tileNumberX; j++) {
				char tile = level.getLevelString().charAt((i * (tileNumberX + 1)) + j);

				if (BOX_CODES.contains(String.valueOf(Character.toLowerCase(tile)))) {
					if (Character.isUpperCase(tile)) {
						movableBoxTiles.add(new MovableBoxTile(j, i, MovableBox.MOVABLE_BOX_COLORS.values()[BOX_CODES.indexOf(Character.toLowerCase(tile))].getID()));
					} else {
						movableBoxes.add(new MovableBox(j, i, 0, MovableBox.MOVABLE_BOX_COLORS.values()[BOX_CODES.indexOf(Character.toLowerCase(tile))].getID()));
					}
				} else if (ROBOT_CODES.contains(String.valueOf(tile))) {
					robots.add(new Robot(j, i, ROBOT_CODES.indexOf(String.valueOf(tile)), true));
				}
			}
		}

		// Resets flag variables
		canWarpBackInTime = true;
		won = false;
		running = true;
		elapsedTime = 0;
	}

	public synchronized void startTimer() {
		// Runs an increment timer every period of time to update the board and robots
		incrementTimer = new Timer("IncrementTimer", false);
		incrementTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				synchronized (Board.this) {
					if (running) {
						elapsedTime += DELAY_PERIOD;

						if (!won) {
							// Update the board with the elapsed time
							update();

							// If the game is won, save the score
							if (won) {
								level.updateScore(new Score(level.getID(), true, getTotalElapsedTime(), robots.size()));

								LevelDatabaseHelper levelHelper = new LevelDatabaseHelper(getApplicationContext());
								levelHelper.updateScore(level);
								levelHelper.close();
							}
						} else {
							// Finish the winning move and end the timer
							update();
						}

						if (won && !getCurrentRobot().isAnimating()) {
							if (gameStateListener != null) {
								gameStateListener.gameEnd();
							}
							this.cancel();
						} else {
							if (gameStateListener != null) {
								gameStateListener.gameUpdate();
							}
						}
					}
				}
			}
		}, DELAY_PERIOD, DELAY_PERIOD);
	}

	public synchronized void stopTimer() {
		incrementTimer.cancel();
	}

	/**
	 * Called whenever the reset time button is pressed. Resets the game to the initial state, but each robots will redo its moves
	 */
	public synchronized void resetTime() {
		addMoveCommandToCurrent(Movable.MOVE_BACK_IN_TIME);

		robots.add(new Robot(getCurrentRobot().getX(), getCurrentRobot().getY(), getCurrentRobot().getDirection(), true));

		for (Movable movable : getMovables()) {
			movable.reset();
		}

		particles.clear();

		elapsedTime = 0;
	}

	/**
	 * Adds the command to move to the current robot whenever the user swipes the screen
	 *
	 * @param direction direction of the swipe
	 */
	public synchronized void addMoveCommandToCurrent(int direction) {
		getCurrentRobot().addMoveCommand(new MoveCommand(direction, elapsedTime));
	}

	/**
	 * Forms most of the game loop. Each time it is called, every robot updates its command list, particles advance in their state, and test flags are updated
	 */
	public synchronized void update() {
		if (!won) {
			// Update robots
			for (Robot robot : robots) {
				MoveCommand moveCommand = robot.updateCommands(elapsedTime);

				if (moveCommand != null) {
					robot.move(moveCommand.getDirection(), tiles, getMovables(), elapsedTime, true);

					if (moveCommand.getDirection() == Movable.MOVE_BACK_IN_TIME) {
						particles.add(new Particle(robot.getX(), robot.getY(), elapsedTime));
					}
				}
			}
		}

		// Update reset time flag and animations
		boolean canWarpFlag = true;

		for (Movable movable : getMovables()) {
			if (movable.getInitX() == getCurrentRobot().getX() && movable.getInitY() == getCurrentRobot().getY()) {
				canWarpFlag = false;
			}

			if (movable.isAnimating()) {
				movable.update(elapsedTime);
			}
		}

		canWarpBackInTime = canWarpFlag;

		// Update particles
		for (final Iterator<Particle> particle = particles.iterator(); particle.hasNext();) {
			if (particle.next().update(elapsedTime)) {
				particle.remove();
			}
		}

		// Check win conditions
		if (!won) {
			boolean testWin = true;
			for (MovableBoxTile tile : movableBoxTiles) {
				boolean tileHasBox = false;
				for (MovableBox movableBox : movableBoxes) {
					if (movableBox.getColor().equals(tile.getColor()) && movableBox.getX() == tile.getX() && movableBox.getY() == tile.getY()) {
						tileHasBox = true;
					}
				}
				if (!tileHasBox) {
					testWin = false;
					break;
				}
			}

			won = testWin;
			if (won) {
				canWarpBackInTime = false;
			}
		}
	}

	/**
	 * Draws the current state of the board to the passed canvas using the given paint
	 *
	 * @param canvas passed canvas to draw to
	 * @param paint paint to use to draw
	 */
	public synchronized void drawBoard(Canvas canvas, Paint paint) {

		try {
			long maxElapsedTime = elapsedTime;

			paint.setColor(getResources().getColor(R.color.text_active));
			paint.setTextSize(getResources().getDimension(R.dimen.rewind_text_size));

			if (!snapshot) {
				bitmapLevelTimeline.draw(canvas);
				for (Robot robot : robots) {
					if (robot.getLastMoveCommand() != null && robot.getLastMoveCommand().getDirection() == Movable.MOVE_BACK_IN_TIME && robot.getLastMoveCommand().getExecuteTime() > maxElapsedTime) {
						maxElapsedTime = robot.getLastMoveCommand().getExecuteTime();
					}
				}

				canvas.drawBitmap(bitmapArray.get(REWIND_ICON + CURRENT),
						(int) (boundsLevelTimeline.left + 1.0 * elapsedTime / maxElapsedTime * boundsLevelTimeline.width() - bitmapArray.get(REWIND_ICON).getWidth() / 2),
						boundsLevelTimeline.exactCenterY() - bitmapArray.get(REWIND_ICON + CURRENT).getHeight() / 2,
						paint);

				canvas.drawText(Score.getTimeMinSec(elapsedTime),
						(int) (boundsLevelTimeline.left + 1.0 * elapsedTime / maxElapsedTime * boundsLevelTimeline.width() - paint.measureText(Score.getTimeMinSec(elapsedTime)) / 2),
						boundsLevelTimeline.bottom,
						paint);
			}

			// Draw pre-rendered floor
			canvas.drawBitmap(bitmapBoardFloor, boundsBoard.left, boundsBoard.top, paint);

			// Draw movable box tiles
			for (MovableBoxTile tile : movableBoxTiles) {
				for (MovableBox movableBox : movableBoxes) {
					if (movableBox.getColor().equals(tile.getColor()) && movableBox.getX() == tile.getX() && movableBox.getY() == tile.getY()) {
						canvas.drawBitmap(bitmapArray.get(TILE_MOVABLE_BOX_ACTIVATED + movableBox.getColor()),
								boundsBoard.left + tile.getX() * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_X_MULTIPLIER,
								boundsBoard.top + tile.getY() * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_Y_MULTIPLIER,
								paint);
					} else {
						canvas.drawBitmap(bitmapArray.get(TILE_MOVABLE_BOX_DEACTIVATED + movableBox.getColor()),
								boundsBoard.left + tile.getX() * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_X_MULTIPLIER,
								boundsBoard.top + tile.getY() * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_Y_MULTIPLIER,
								paint);
					}
				}
			}

			// Draw robots

			for (Robot robot : robots) {
				if (robot.isVisible()) {

					for (int i = 0; i < (snapshot ? 1 : THREE_DIMENSIONAL_VALUE / 2); i++) {
						rotateMatrix.reset();

						rotateImage(rotateMatrix, bitmapArray.get(robot.equals(getCurrentRobot()) ? ROBOT + CURRENT : ROBOT), robot.getViewDirection() * 90);
						rotateMatrix.postTranslate(boundsBoard.left + robot.getViewX() * drawTileSize - i * THREE_DIMENSIONAL_X_MULTIPLIER + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_X_MULTIPLIER,
								boundsBoard.top + robot.getViewY() * drawTileSize - i * THREE_DIMENSIONAL_Y_MULTIPLIER + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_Y_MULTIPLIER);
						canvas.drawBitmap(bitmapArray.get(robot.equals(getCurrentRobot()) ? ROBOT + CURRENT : ROBOT), rotateMatrix, paint);
					}
				}

				if (!snapshot && robot.getLastMoveCommand() != null && robot.getLastMoveCommand().getDirection() == Movable.MOVE_BACK_IN_TIME) {
					canvas.drawBitmap(bitmapArray.get(REWIND_ICON),
							(int) (boundsLevelTimeline.left + 1.0 * robot.getLastMoveCommand().getExecuteTime() / maxElapsedTime * boundsLevelTimeline.width() - bitmapArray.get(REWIND_ICON).getWidth() / 2),
							boundsLevelTimeline.exactCenterY() - bitmapArray.get(REWIND_ICON).getHeight() / 2,
							paint);

					canvas.drawText(Score.getTimeMinSec(robot.getLastMoveCommand().getExecuteTime()),
							(int) (boundsLevelTimeline.left + 1.0 * robot.getLastMoveCommand().getExecuteTime() / maxElapsedTime * boundsLevelTimeline.width() - paint.measureText(Score.getTimeMinSec(robot.getLastMoveCommand().getExecuteTime())) / 2),
							boundsLevelTimeline.bottom,
							paint);
				}
			}

			// Draw movable boxes
			for (MovableBox movableBox : movableBoxes) {
				if (movableBox.isVisible()) {
					for (int i = 0; i < (snapshot ? 1 : THREE_DIMENSIONAL_VALUE / 2); i++) {
						canvas.drawBitmap(bitmapArray.get(MOVABLE_BOX + movableBox.getColor()),
								boundsBoard.left + movableBox.getViewX() * drawTileSize - i * THREE_DIMENSIONAL_X_MULTIPLIER + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_X_MULTIPLIER,
								boundsBoard.top + movableBox.getViewY() * drawTileSize - i * THREE_DIMENSIONAL_Y_MULTIPLIER + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_Y_MULTIPLIER,
								paint);
					}
				}
			}

			// Draw particles
			for (Particle particle : particles) {
				paint.setAlpha(255 - (int) (particle.getPercentDone() * 255));
				canvas.drawBitmap(bitmapArray.get(PARTICLE),
						boundsBoard.left + particle.getX() * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_X_MULTIPLIER,
						boundsBoard.top + particle.getY() * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_Y_MULTIPLIER,
						paint);
			}
			paint.setAlpha(255);

			// Draw pre-rendered walls
			canvas.drawBitmap(bitmapBoardWalls, boundsBoard.left, boundsBoard.top, paint);

		} catch (NullPointerException e) {

			// If level hasn't been loaded yet
			paint.setColor(getResources().getColor(R.color.background));
			canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

			paint.setColor(getResources().getColor(R.color.text_active));

			canvas.drawText(getResources().getText(R.string.loading).toString(), canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
		}
	}

	/**
	 * Initializes the images used for drawing the board with the given dimensions and paint
	 *
	 * @param width pixel width of the space given to draw
	 * @param height pixel height of the space given to draw
	 * @param paint paint to use to draw
	 */
	public synchronized void initializeBitmaps(int width, int height, Paint paint) {
		// Initializes the bitmap array
		bitmapArray = new HashMap<String, Bitmap>();

		if (snapshot) {
			boundsBoard = new Rect(0, 0, width, height);
		} else {
			boundsLevelTimeline = new Rect(getResources().getDimensionPixelSize(R.dimen.rewind_timeline_padding), 0, width - getResources().getDimensionPixelSize(R.dimen.rewind_timeline_padding), getResources().getDimensionPixelSize(R.dimen.rewind_timeline_height));

			bitmapLevelTimeline = (NinePatchDrawable) getResources().getDrawable(R.drawable.level_timeline);
			bitmapLevelTimeline.setBounds(boundsLevelTimeline);

			boundsBoard = new Rect(0, boundsLevelTimeline.bottom, width, height - boundsLevelTimeline.height());
		}

		int drawBorderSize = getResources().getDimensionPixelSize(R.dimen.board_padding);
		// Sets the tile size that will be used
		if (boundsBoard.width() / tileNumberX < boundsBoard.height() / tileNumberY ) {
			drawTileSize = (boundsBoard.width() - drawBorderSize * 2) / tileNumberX;
		} else {
			drawTileSize = (boundsBoard.height() - drawBorderSize * 2) / tileNumberY;
		}

		// Initializes a rectangle for less calculations on where to draw
		boundsBoard.set((width - drawTileSize * tileNumberX) / 2, (height - drawTileSize * tileNumberY) / 2, drawTileSize * tileNumberX, drawTileSize * tileNumberY);

		// Puts each bitmap into a map using labels
		// Wall bitmaps
		bitmapArray.put(Tile.TILE_WALL + Tile.ALONE_LABEL, createScaledBitmap(R.drawable.tile_wall_alone, drawTileSize));

		bitmapArray.put(Tile.TILE_WALL + Tile.ENDPOINT_LABEL + Tile.UP, createScaledBitmap(R.drawable.tile_wall_endpoint_up, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.ENDPOINT_LABEL + Tile.RIGHT, createScaledBitmap(R.drawable.tile_wall_endpoint_right, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.ENDPOINT_LABEL + Tile.DOWN, createScaledBitmap(R.drawable.tile_wall_endpoint_down, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.ENDPOINT_LABEL + Tile.LEFT, createScaledBitmap(R.drawable.tile_wall_endpoint_left, drawTileSize));

		bitmapArray.put(Tile.TILE_WALL + Tile.MIDDLE_LABEL + Tile.UP_DOWN, createScaledBitmap(R.drawable.tile_wall_middle_up_down, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.MIDDLE_LABEL + Tile.LEFT_RIGHT, createScaledBitmap(R.drawable.tile_wall_middle_left_right, drawTileSize));

		bitmapArray.put(Tile.TILE_WALL + Tile.CORNER_LABEL + Tile.UP, createScaledBitmap(R.drawable.tile_wall_corner_up, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.CORNER_LABEL + Tile.RIGHT, createScaledBitmap(R.drawable.tile_wall_corner_right, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.CORNER_LABEL + Tile.DOWN, createScaledBitmap(R.drawable.tile_wall_corner_down, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.CORNER_LABEL + Tile.LEFT, createScaledBitmap(R.drawable.tile_wall_corner_left, drawTileSize));

		bitmapArray.put(Tile.TILE_WALL + Tile.T_LABEL + Tile.UP, createScaledBitmap(R.drawable.tile_wall_t_up, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.T_LABEL + Tile.RIGHT, createScaledBitmap(R.drawable.tile_wall_t_right, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.T_LABEL + Tile.DOWN, createScaledBitmap(R.drawable.tile_wall_t_down, drawTileSize));
		bitmapArray.put(Tile.TILE_WALL + Tile.T_LABEL + Tile.LEFT, createScaledBitmap(R.drawable.tile_wall_t_left, drawTileSize));

		bitmapArray.put(Tile.TILE_WALL + Tile.CROSS_LABEL, createScaledBitmap(R.drawable.tile_wall_cross, drawTileSize));

		// Floor bitmaps
		bitmapArray.put(Tile.TILE_FLOOR + Tile.ALONE_LABEL, createScaledBitmap(R.drawable.tile_floor_alone, drawTileSize));

		bitmapArray.put(Tile.TILE_FLOOR + Tile.ENDPOINT_LABEL + Tile.UP, createScaledBitmap(R.drawable.tile_floor_endpoint_up, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.ENDPOINT_LABEL + Tile.RIGHT, createScaledBitmap(R.drawable.tile_floor_endpoint_right, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.ENDPOINT_LABEL + Tile.DOWN, createScaledBitmap(R.drawable.tile_floor_endpoint_down, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.ENDPOINT_LABEL + Tile.LEFT, createScaledBitmap(R.drawable.tile_floor_endpoint_left, drawTileSize));

		bitmapArray.put(Tile.TILE_FLOOR + Tile.MIDDLE_LABEL + Tile.UP_DOWN, createScaledBitmap(R.drawable.tile_floor_middle_up_down, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.MIDDLE_LABEL + Tile.LEFT_RIGHT, createScaledBitmap(R.drawable.tile_floor_middle_left_right, drawTileSize));

		bitmapArray.put(Tile.TILE_FLOOR + Tile.CORNER_LABEL + Tile.UP, createScaledBitmap(R.drawable.tile_floor_corner_up, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.CORNER_LABEL + Tile.RIGHT, createScaledBitmap(R.drawable.tile_floor_corner_right, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.CORNER_LABEL + Tile.DOWN, createScaledBitmap(R.drawable.tile_floor_corner_down, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.CORNER_LABEL + Tile.LEFT, createScaledBitmap(R.drawable.tile_floor_corner_left, drawTileSize));

		bitmapArray.put(Tile.TILE_FLOOR + Tile.T_LABEL + Tile.UP, createScaledBitmap(R.drawable.tile_floor_t_up, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.T_LABEL + Tile.RIGHT, createScaledBitmap(R.drawable.tile_floor_t_right, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.T_LABEL + Tile.DOWN, createScaledBitmap(R.drawable.tile_floor_t_down, drawTileSize));
		bitmapArray.put(Tile.TILE_FLOOR + Tile.T_LABEL + Tile.LEFT, createScaledBitmap(R.drawable.tile_floor_t_left, drawTileSize));

		bitmapArray.put(Tile.TILE_FLOOR + Tile.CROSS_LABEL, createScaledBitmap(R.drawable.tile_floor_cross, drawTileSize));

		bitmapArray.put(TILE_MOVABLE_BOX_DEACTIVATED + MovableBox.MOVABLE_BOX_COLORS.YELLOW.getID(), createScaledBitmap(R.drawable.tile_movable_box_yellow_deactivated, drawTileSize));
		bitmapArray.put(TILE_MOVABLE_BOX_ACTIVATED + MovableBox.MOVABLE_BOX_COLORS.YELLOW.getID(), createScaledBitmap(R.drawable.tile_movable_box_yellow_activated, drawTileSize));

		bitmapArray.put(ROBOT, createScaledBitmap(R.drawable.robot, drawTileSize));
		bitmapArray.put(ROBOT + CURRENT, createScaledBitmap(R.drawable.robot_current, drawTileSize));

		bitmapArray.put(MOVABLE_BOX + MovableBox.MOVABLE_BOX_COLORS.YELLOW.getID(), createScaledBitmap(R.drawable.movable_box_yellow, drawTileSize));

		bitmapArray.put(PARTICLE, createScaledBitmap(R.drawable.particle, drawTileSize));

		bitmapArray.put(REWIND_ICON, createScaledBitmap(R.drawable.rewind_icon, getResources().getDimensionPixelSize(R.dimen.rewind_icon_size)));
		bitmapArray.put(REWIND_ICON + CURRENT, createScaledBitmap(R.drawable.rewind_icon_current, getResources().getDimensionPixelSize(R.dimen.rewind_icon_size)));

		// Pre draws the floor so that this will only need to be done once
		bitmapBoardFloor = Bitmap.createBitmap(boundsBoard.left + (tileNumberX * drawTileSize), boundsBoard.top + (tileNumberY * drawTileSize), Bitmap.Config.ARGB_8888);
		Canvas tempCanvas = new Canvas(bitmapBoardFloor);

		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				tempCanvas.drawBitmap(bitmapArray.get(tiles[i][j].getDisplayID()),
						j * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_X_MULTIPLIER,
						i * drawTileSize + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_Y_MULTIPLIER,
						paint);
			}
		}

		// Pre draw the walls
		bitmapBoardWalls = Bitmap.createBitmap(boundsBoard.left + (tileNumberX * drawTileSize), boundsBoard.top + (tileNumberY * drawTileSize), Bitmap.Config.ARGB_8888);
		tempCanvas = new Canvas(bitmapBoardWalls);
		for (int i = 0; i < tiles.length; i++) {
			for (int j = 0; j < tiles[i].length; j++) {
				if (tiles[i][j].getID().equals(Tile.TILE_WALL)) {
					for (int k = 0; k < (snapshot ? 1 : THREE_DIMENSIONAL_VALUE); k++) {
						tempCanvas.drawBitmap(bitmapArray.get(tiles[i][j].getDisplayID()),
								j * drawTileSize - k * THREE_DIMENSIONAL_X_MULTIPLIER + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_X_MULTIPLIER,
								i * drawTileSize - k * THREE_DIMENSIONAL_X_MULTIPLIER + THREE_DIMENSIONAL_VALUE * THREE_DIMENSIONAL_Y_MULTIPLIER,
								paint);
					}
				}
			}
		}
	}

	/**
	 * Rotates the image the specified number of degrees using the passed matrix
	 *
	 * @param matrix matrix used for drawing
	 * @param bitmap image to be rotated
	 * @param angleDegrees angled to be rotated in degrees
	 * @return updated version of the matrix used for drawing
	 */
	private static Matrix rotateImage(Matrix matrix, Bitmap bitmap, float angleDegrees) {
		matrix.postTranslate(-bitmap.getWidth() / 2, -bitmap.getHeight() / 2);
		matrix.postRotate(angleDegrees);
		matrix.postTranslate(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
		return matrix;
	}

	/**
	 * Creates a bitmap from the given id that is scaled the tile size for the board
	 *
	 * @param resourceID resource id that will be passed to the resources
	 * @return created, scaled bitmap
	 */
	private Bitmap createScaledBitmap(int resourceID, int size) {
		return Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), resourceID), size, size, false);
	}

	/**
	 * Parses the level string to get the number of columns
	 *
	 * @param level level string as given by Level.getLevelString()
	 * @return number of columns for the given level
	 */
	public static int getTileNumberX(String level) {
		return level.indexOf("_");
	}

	/**
	 * Parses the level string to get the number of rows
	 *
	 * @param level level string as given by Level.getLevelString()
	 * @return number of rows for the given level
	 */
	public static int getTileNumberY(String level) {
		return (level.lastIndexOf("_") + 1) / (getTileNumberX(level) + 1);
	}

	/**
	 * @return the robot that is currently being controlled
	 */
	public synchronized Robot getCurrentRobot() {
		return robots.get(robots.size() - 1);
	}

	/**
	 * @return the robot array (all robots in the current game)
	 */
	public synchronized ArrayList<Robot> getRobots() {
		return robots;
	}

	/**
	 * @return the flag showing if it is legal to reset the time
	 */
	public synchronized boolean canWarpBackInTime() {
		return canWarpBackInTime && !won;
	}

	/**
	 * @return the totalElapsedTime from all robot states in milliseconds
	 */
	private synchronized long getTotalElapsedTime() {
		long totalElapsedTime = 0;
		for (Robot robot : robots) {
			totalElapsedTime += robot.getLastMoveCommand().getExecuteTime();
		}

		return totalElapsedTime;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * @return the flag showing if the game is won
	 */
	public synchronized boolean haveWon() {
		return won;
	}

	/**
	 * @return an array of all movable objects on the board
	 */
	public synchronized ArrayList<Movable> getMovables() {
		ArrayList<Movable> movables = new ArrayList<Movable>();

		movables.addAll(robots);
		movables.addAll(movableBoxes);

		return movables;
	}

	// Constants that control the fake 3D effect that is drawn
	private static final int THREE_DIMENSIONAL_VALUE = 8;
	private static final float THREE_DIMENSIONAL_X_MULTIPLIER = 1.0f;
	private static final float THREE_DIMENSIONAL_Y_MULTIPLIER = 1.0f;

	// Constants for identifying other objects
	public static final String ROBOT = "ROBOT";
	public static final String CURRENT = "_CURRENT";
	public static final String TILE_MOVABLE_BOX_DEACTIVATED = "TILE_MOVABLE_BOX_DEACTIVATED_";
	public static final String TILE_MOVABLE_BOX_ACTIVATED = "TILE_MOVABLE_BOX_ACTIVATED_";
	public static final String MOVABLE_BOX= "MOVABLE_BOX_";
	public static final String PARTICLE = "PARTICLE";
	public static final String REWIND_ICON = "REWIND_ICON";

	// Constants for decoding level strings
	private static final String BOX_CODES = "a";
	private static final char WALL_CODE = '1';
	private static final String ROBOT_CODES = "^>v<";

	/**
	 * Delay for the update thread in milliseconds
	 */
	private static final long DELAY_PERIOD = 5;

	private static final String LOG_TAG = "Board";
}
