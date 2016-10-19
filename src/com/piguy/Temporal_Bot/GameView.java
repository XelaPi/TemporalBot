package com.piguy.Temporal_Bot;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class that wraps the board and manages its updates
 *
 * @author Alex Vanyo
 */
public class GameView extends UpdateView {

    public Board board;
    private Paint paint;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();

        board = new Board(context);
    }

    /**
     * Paint the board
     *
     * @param canvas canvas to draw to
     */
    @Override
    public void paint(Canvas canvas) {
        super.paint(canvas);
        board.drawBoard(canvas, paint);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.surfaceChanged(holder, format, width, height);
        board.initializeBitmaps(width, height, paint);
    }

    private static final String LOG_TAG = "GameView";
}
