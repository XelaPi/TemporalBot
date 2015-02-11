package com.piguy.Temporal_Bot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity for the game, managing the game view, buttons, and menus
 *
 * @author Alex Vanyo
 */
public class GameActivity extends Activity implements GestureDetector.OnGestureListener, GameStateListener {

	private GameView gameView;
	private ToggleButton resetTimeButton;
	private LinearLayout pauseMenu;
	private TextView instructionView;
	private LinearLayout instructionLayout;

	private GestureDetector gestureDetector;

	/**
	 * Initializes the activity. It collects the level info from the previous activity, and initializes the game
	 *
	 * @param savedInstanceState previous state of the activity
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.game);
		this.overridePendingTransition(R.anim.slide_enter_from_right, R.anim.slide_exit_to_left);

		Level level = this.getIntent().getExtras().getParcelable(LevelMenuActivity.INTENT_EXTRA_LEVEL);

		gestureDetector = new GestureDetector(this, this);

		((Button) this.findViewById(R.id.menu_button)).setText(level.getName());

		resetTimeButton = (ToggleButton) this.findViewById(R.id.reset_time_button);
		gameView = (GameView) this.findViewById(R.id.game_view);
		gameView.board.setLevel(level, false);
		gameView.board.setGameStateListener(this);

		this.reset();

		pauseMenu = (LinearLayout) this.findViewById(R.id.pause_layout);
		instructionView = (TextView) this.findViewById(R.id.instruction_view);
		instructionLayout = (LinearLayout) this.findViewById(R.id.instruction_layout);

		// Show the instructions if the level has them
		if (level.hasInstruction() && !level.getScore().getCompleted()) {
			showInstruction(level.getInstruction());
		}
	}

	@Override
	public boolean onKeyDown(int keycode, KeyEvent event ) {
		if(keycode == KeyEvent.KEYCODE_MENU){
			this.onBackPressed();
		}
		return super.onKeyDown(keycode, event);
	}

	/**
	 * Resets the level completely
	 */
	private void reset() {
		gameView.board.reset();
	}

	/**
	 * Called whenever the reset time button is pressed. Resets the game view and the timer
	 */
	private void resetTime() {
		gameView.board.resetTime();
	}

	@Override
	public void onResume() {
		super.onResume();

		gameView.board.startTimer();
	}

	@Override
	public void onPause() {
		super.onPause();

		gameView.board.stopTimer();
	}

	@Override
	public void gameStart() {

	}

	@Override
	public void gameUpdate() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				resetTimeButton.setEnabled(gameView.board.canWarpBackInTime());
			}
		});
	}

	@Override
	public void gameEnd() {
		pause(null);
	}


	@Override
	public boolean onTouchEvent(MotionEvent e) {
		this.gestureDetector.onTouchEvent(e);

		return super.onTouchEvent(e);
	}

	@Override
	public boolean onDown(MotionEvent event) {
		//Log.d(LOG_TAG,"onDown: " + event.toString());
		return true;
	}

	/**
	 * Calculates if there was a swipe, and if so, add that to the robot in the game view
	 *
	 * @param event1 starting event
	 * @param event2 ending event
	 * @param velocityX velocity of the fling in the x direction
	 * @param velocityY velocity of the fling in the y direction
	 */
	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
		//Log.d(LOG_TAG, "onFling: " + event1.getX() + ":" + event1.getY() + " , " + event2.getX() + ":" + event2.getY());

		float xDifference = event2.getX() - event1.getX();
		float yDifference = event2.getY() - event1.getY();

		//Log.d(LOG_TAG, xDifference + " : " + yDifference);
		//Log.d(LOG_TAG, velocityX + " : " + velocityY);
		if (!gameView.board.haveWon()) {
			if (Math.abs(xDifference) > Math.abs(yDifference)) {
				gameView.board.addMoveCommandToCurrent(xDifference > 0 ? Movable.MOVE_RIGHT : Movable.MOVE_LEFT);
			} else {
				gameView.board.addMoveCommandToCurrent(yDifference > 0 ? Movable.MOVE_DOWN : Movable.MOVE_UP);
			}

		}

		return true;
	}

	@Override
	public void onLongPress(MotionEvent event) {
		//Log.d(LOG_TAG, "onLongPress: " + event.toString());
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		//Log.d(LOG_TAG, "onScroll: " + e1.toString()+e2.toString());
		return true;
	}

	@Override
	public void onShowPress(MotionEvent event) {
		//Log.d(LOG_TAG, "onShowPress: " + event.toString());
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event) {
		//Log.d(LOG_TAG, "onSingleTapUp: " + event.toString());
		return true;
	}

	/**
	 * Overrides onBackPressed to open or close the menu screen
	 */
	@Override
	public void onBackPressed() {
		if (gameView.board.isRunning()) {
			pause(null);
		} else {
			if (pauseMenu.getVisibility() == View.VISIBLE) {
				resume(null);
			} else if (instructionLayout.getVisibility() == View.VISIBLE) {
				hideInstruction(null);
			}
		}
	}

	/**
	 * Finishes this activity and return to the menu outside the game
	 *
	 * @param view view that called this function
	 */
	public void back(View view) {
		super.onBackPressed();

		this.overridePendingTransition(R.anim.slide_enter_from_left, R.anim.slide_exit_to_right);
	}

	/**
	 * Pauses the game and shows the pause menu
	 *
	 * @param view view that called this function
	 */
	public void pause(View view) {
		gameView.board.setRunning(false);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				pauseMenu.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * Resumes the game and hides the pause menu
	 *
	 * @param view view that called this function
	 */
	public void resume(View view) {
		gameView.board.setRunning(true);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				pauseMenu.setVisibility(View.GONE);
			}
		});
	}

	/**
	 * Pauses the game and shows the instruction view
	 *
	 * @param instruction instruction string to be shown
	 */
	public void showInstruction(final String instruction) {
		gameView.board.setRunning(false);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				instructionView.setText(instruction);
				instructionLayout.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * Resumes the game and hides the instruction view
	 *
	 * @param view view that called this function
	 */
	public void hideInstruction(View view) {
		gameView.board.setRunning(true);

		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				instructionLayout.setVisibility(View.GONE);
			}
		});
	}

	/**
	 * Restarts the game completely, all robots are reset
	 *
	 * @param view view that called this function
	 */
	public void restart(View view) {
		reset();
		this.resume(view);
	}

	/**
	 * Resets the time, increasing the number of robots
	 *
	 * @param view view that called this function
	 */
	public void resetTime(View view) {
		if (gameView.board.isRunning()) {
			resetTime();
		}
	}

	private static final String LOG_TAG = "GameActivity";
}
