package com.piguy.Temporal_Bot;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.ArrayList;

/**
 * Menu activity that displays all of the level buttons for starting a game
 *
 * @author Alex Vanyo
 */
public class LevelMenuActivity extends Activity {

	public static final String INTENT_EXTRA_LEVEL = "INTENT_EXTRA_LEVEL";

	private LinearLayout levelButtonsLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.level_menu);

		levelButtonsLayout = (LinearLayout) this.findViewById(R.id.level_buttons_layout);

		addLevelButtons();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateLevelButtons();
	}

	/**
	 * Adds all of the levels found in the database by creating a LevelButton for each one
	 */
	private void addLevelButtons() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				LevelDatabaseHelper levelDatabaseHelper = new LevelDatabaseHelper(getApplicationContext());

				final ArrayList<Level> levels = levelDatabaseHelper.getAllLevels();

				levelDatabaseHelper.close();

				for (int i = 0; i < levels.size(); i++) {

					final int levelIndex = i;

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							final LevelButton levelButton = (LevelButton) getLayoutInflater().inflate(R.layout.level_button, null);

							new Thread(new Runnable() {
								@Override
								public void run() {
									levelButton.updateViewWithArguments(levels.get(levelIndex));
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											addViewInOrder(levelButton);
											levelButton.updateViewUI();
										}
									});
								}
							}).start();
						}
					});
				}

			}
		}).start();
	}

	/**
	 * Updates the level buttons if there has been a change in the levels or scores
	 */
	private void updateLevelButtons() {

		// TODO: Only update the level that changed
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < levelButtonsLayout.getChildCount(); i++) {
					final int levelIndex = i;

					((LevelButton) levelButtonsLayout.getChildAt(levelIndex)).updateLevel();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							((LevelButton) levelButtonsLayout.getChildAt(levelIndex)).updateViewUI();
						}
					});
				}
			}
		};
		new Thread(runnable).start();
	}

	/**
	 * Adds each level button to the correct spot in the list. This is to prevent scrambling due to threads finishing at different times
	 *
	 * @param levelButton level button that is going to be added
	 */
	private void addViewInOrder(LevelButton levelButton) {
		if (levelButtonsLayout.getChildCount() == 0) {
			levelButtonsLayout.addView(levelButton);
		} else {
			int testIndex = levelButtonsLayout.getChildCount();
			while (((LevelButton) levelButtonsLayout.getChildAt(testIndex - 1)).getLevelID() > levelButton.getLevelID() || testIndex == 0) {
				testIndex--;
			}
			levelButtonsLayout.addView(levelButton, testIndex);
		}
	}

	private static final String LOG_TAG = "MenuActivity";
}
