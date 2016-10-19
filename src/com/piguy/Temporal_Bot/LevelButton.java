package com.piguy.Temporal_Bot;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Wrapper for each level button in the level_menu. Contains the thumbnail, stars, description, and start button
 *
 * @author Alex Vanyo
 */
public class LevelButton extends LinearLayout {

    private Level level;

    private LinearLayout wrapper_layout;
    private LinearLayout levelButtonNonExpandWrapper;
    private TextView levelName;
    private ImageView completedImageView;
    private ImageView timeImageView;
    private ImageView robotsImageView;

    private ArrayList<ImageView[]> starImageViews;

    private LinearLayout levelButtonExpandWrapper;
    private GamePreview levelThumbnail;
    private Button levelButton;
    private TextView textCompleted;
    private TextView textTime;
    private TextView textTargetTime;
    private TextView textRobots;
    private TextView textTargetRobots;

    private boolean expanded;

    private int[] wrapperLocation = new int[2];
    int[] tempLocation = new int[2];
    int[] tempLocation2 = new int[2];
    private int collapsedHeight;
    private int expandedHeight;

    public LevelButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.expanded = false;

        // Provides listeners for when the button is clicked to expand and collapse it
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expanded) {
                    v.startAnimation(new ExpandAnimation(v, starImageViews, collapsedHeight));
                } else {
                    v.startAnimation(new ExpandAnimation(v, starImageViews, expandedHeight));
                }

                expanded ^= true;
            }
        });
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        wrapper_layout.getLocationInWindow(wrapperLocation);
        if (changed) {
            for (ImageView[] imageViewSet : starImageViews) {
                imageViewSet[expanded ? 2 : 1].getLocationOnScreen(tempLocation);
                tempLocation = new int[]{tempLocation[0] - wrapperLocation[0], tempLocation[1] - wrapperLocation[1]};

                imageViewSet[0].setTranslationX(tempLocation[0]);
                imageViewSet[0].setTranslationY(tempLocation[1]);
            }
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();

        this.wrapper_layout = (LinearLayout) this.findViewById(R.id.wrapper_layout);
        this.levelButtonNonExpandWrapper = (LinearLayout) this.findViewById(R.id.level_button_non_expand_wrapper);
        this.levelName = (TextView) this.findViewById(R.id.level_name);
        this.completedImageView = (ImageView) this.findViewById(R.id.completed_image_view);
        this.timeImageView = (ImageView) this.findViewById(R.id.time_image_view);
        this.robotsImageView = (ImageView) this.findViewById(R.id.robots_image_view);

        starImageViews = new ArrayList<ImageView[]>();
        starImageViews.add(new ImageView[]{completedImageView, (ImageView) this.findViewById(R.id.completed_pos_0), (ImageView) this.findViewById(R.id.completed_pos_1)});
        starImageViews.add(new ImageView[]{timeImageView, (ImageView) this.findViewById(R.id.time_pos_0), (ImageView) this.findViewById(R.id.time_pos_1)});
        starImageViews.add(new ImageView[]{robotsImageView, (ImageView) this.findViewById(R.id.robots_pos_0), (ImageView) this.findViewById(R.id.robots_pos_1)});

        this.levelButtonExpandWrapper = (LinearLayout) this.findViewById(R.id.level_button_expand_wrapper);
        this.levelThumbnail = (GamePreview) this.findViewById(R.id.level_thumbnail);
        this.levelButton = (Button) this.findViewById(R.id.level_button);
        this.textCompleted = (TextView) this.findViewById(R.id.text_completed);
        this.textTime = (TextView) this.findViewById(R.id.text_time);
        this.textTargetTime = (TextView) this.findViewById(R.id.text_target_time);
        this.textRobots = (TextView) this.findViewById(R.id.text_robots);
        this.textTargetRobots = (TextView) this.findViewById(R.id.text_target_robots);

        this.measure(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        expandedHeight = this.getMeasuredHeight();
        levelButtonExpandWrapper.measure(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        collapsedHeight = expandedHeight - levelButtonExpandWrapper.getMeasuredHeight();

        if (!isInEditMode()) {
            levelButtonExpandWrapper.setVisibility(GONE);
        } else {
            updateViewWithArguments(Level.DEFAULT_LEVEL);
            updateViewUI();
        }
    }

    /**
     * Updates the level button with the actual level
     *
     * @param argLevel level that the button represents
     */
    public void updateViewWithArguments(Level argLevel) {
        this.level = argLevel;

        levelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starts a game with this information
                Intent gameIntent = new Intent(getContext(), GameActivity.class);
                gameIntent.putExtra(LevelMenuActivity.INTENT_EXTRA_LEVEL, level);
                getContext().startActivity(gameIntent);
            }
        });
    }

    /**
     * Updates the level button's ui, so that it shows the new information
     */
    public void updateViewUI() {
        levelName.setText(level.getName());

        if (level.getScore().getCompleted()) {
            completedImageView.setImageDrawable(getResources().getDrawable(R.drawable.star_complete));

            if (level.getScore().getTime() <= level.getTargetTime()) {
                timeImageView.setImageDrawable(getResources().getDrawable(R.drawable.star_complete));
            }
            if (level.getScore().getRobots() <= level.getTargetRobots()) {
                robotsImageView.setImageDrawable(getResources().getDrawable(R.drawable.star_complete));
            }
        }

        levelThumbnail.setLevel(level);
        levelThumbnail.invalidate();

        textCompleted.setText(level.getScore().getCompleted() ? getResources().getText(R.string.text_completed) : getResources().getText(R.string.text_incomplete));
        textCompleted.setTextColor(level.getScore().getCompleted() ? getResources().getColor(R.color.text_completed) : getResources().getColor(R.color.text_incomplete));
        textTime.setText("" + (level.getScore().getCompleted() ? Score.getTimeMinSec(level.getScore().getTime()) : getResources().getText(R.string.text_default_time)));
        textTime.setTextColor(level.getScore().getTime() <= level.getTargetTime() ? getResources().getColor(R.color.text_completed) : getResources().getColor(R.color.text_incomplete));
        textTargetTime.setText(Score.getTimeMinSec(level.getTargetTime()));
        textRobots.setText("" + (level.getScore().getCompleted() ? level.getScore().getRobots() : getResources().getText(R.string.text_default_robots)));
        textRobots.setTextColor(level.getScore().getRobots() <= level.getTargetRobots() ? getResources().getColor(R.color.text_completed) : getResources().getColor(R.color.text_incomplete));
        textTargetRobots.setText("" + level.getTargetRobots());
    }

    /**
     * Updates the button with new level information
     */
    public void updateLevel() {
        LevelDatabaseHelper levelDatabaseHelper = new LevelDatabaseHelper(getContext());

        updateViewWithArguments(levelDatabaseHelper.getLevel(level.getID()));
    }

    /**
     * Provides the animation for closing and opening the button when clicked on
     */
    private class ExpandAnimation extends Animation implements Animation.AnimationListener {

        private View v;
        private ArrayList<ImageView[]> imageViews = new ArrayList<ImageView[]>();
        private int targetHeight;
        private int expandHeight;

        private ExpandAnimation(View v, ArrayList<ImageView[]> imageViews, int targetHeight) {
            this.v = v;
            this.imageViews = imageViews;
            this.targetHeight = targetHeight;
            this.expandHeight = targetHeight - v.getMeasuredHeight();

            this.setAnimationListener(this);
            this.setDuration(ANIMATION_LENGTH);
        }

        /**
         * Smooth adjustment in position based on a logistic growth model
         *
         * @param time Interpolated Time from 0 to 1
         * @return Adjusted interpolated factor of multiplication
         */
        private float movementTransform(float time) {
            return (float) (1 / (1 + Math.pow(Math.E, 2 * (time - 0.5) * ANIMATION_APPROXIMATION)));
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            // Updates the height of the layout
            v.getLayoutParams().height = (int) (targetHeight + expandHeight * (movementTransform(interpolatedTime) - 1));
            for (ImageView[] imageViewSet : imageViews) {
                imageViewSet[expanded ? 2 : 1].getLocationInWindow(tempLocation);
                imageViewSet[expanded ? 1 : 2].getLocationInWindow(tempLocation2);

                imageViewSet[0].setTranslationX(tempLocation2[0] + movementTransform(interpolatedTime) * (tempLocation[0] - tempLocation2[0]) - wrapperLocation[0]);
                imageViewSet[0].setTranslationY(tempLocation2[1] + movementTransform(interpolatedTime) * (tempLocation[1] - tempLocation2[1]) - wrapperLocation[1]);
            }
            v.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            if (levelButtonExpandWrapper.getVisibility() == GONE) {
                levelButtonExpandWrapper.setVisibility(VISIBLE);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            v.getLayoutParams().height = targetHeight;
            v.requestLayout();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        /**
         * Length of the expand animation in milliseconds
         */
        private static final long ANIMATION_LENGTH = 300;

        private final double ANIMATION_APPROXIMATION = Math.log(0.01);
    }

    /**
     * @return the level id for this button
     */
    public int getLevelID() {
        return level.getID();
    }

    private static final String LOG_TAG = "LevelButton";
}
