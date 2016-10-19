package com.piguy.Temporal_Bot;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.TimeUnit;

/**
 * Custom view for updating a surface view
 *
 * @author Alex Vanyo
 */
public class UpdateView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Paint paint;

    private Thread updateThread;
    private BitmapDrawable bitmapBackground;

    public UpdateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();

        getHolder().addCallback(this);
    }

    /**
     * Paint the background onto the canvas
     *
     * @param canvas canvas to draw onto
     */
    public void paint(Canvas canvas) {
        try {

            bitmapBackground.draw(canvas);

        } catch (NullPointerException e) {

            paint.setColor(getResources().getColor(R.color.background));
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);

            paint.setColor(getResources().getColor(R.color.text_active));

            canvas.drawText(getResources().getText(R.string.loading).toString(), canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!isInEditMode()) {
            updateThread = new Thread(this);
            updateThread.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        bitmapBackground = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.background_tile));
        bitmapBackground.setBounds(0, 0, width, height);
        bitmapBackground.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!isInEditMode()) {
            updateThread.interrupt();
        }
    }

    @Override
    public void run() {
        Canvas canvas;
        long beginTimeMillis, timeTakenMillis, timeLeftMillis;
        while (!Thread.currentThread().isInterrupted()) {

            canvas = null;

            // Get the time before updates/draw
            beginTimeMillis = System.currentTimeMillis();

            // Lock onto the holder and draw onto its canvas
            try {
                synchronized (getHolder()) {
                    canvas = getHolder().lockCanvas();
                    if (canvas != null) {
                        paint(canvas);
                    }
                }
            } finally {

                if (canvas != null) {
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }

            // Calculate how long the system should wait
            timeTakenMillis = System.currentTimeMillis() - beginTimeMillis;
            timeLeftMillis = (1000 / TARGET_FPS) - timeTakenMillis;

            if (timeLeftMillis < 5) {
                timeLeftMillis = 5;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(timeLeftMillis);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final int TARGET_FPS = 30;

    private static final String LOG_TAG = "UpdateView";
}
