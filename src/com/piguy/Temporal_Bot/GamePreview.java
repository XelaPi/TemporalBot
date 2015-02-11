package com.piguy.Temporal_Bot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class that displays a thumbnail of the game board
 *
 * @author Alex Vanyo
 */
public class GamePreview extends ImageView {

	private Board board;

	private Paint paint;

	public GamePreview(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.board = new Board(context);

		this.paint = new Paint();
	}

	/**
	 * Sets the level that the preview will show and creates/sets its bitmap to be that level
	 *
	 * @param level level for the thumbnail
	 */
	public void setLevel(Level level) {
		board.setLevel(level);
		board.initializeBitmaps(this.getMeasuredWidth(), this.getMeasuredHeight(), paint);

		Bitmap boardBitmap = Bitmap.createBitmap(this.getMeasuredWidth(), this.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		board.drawBoard(new Canvas(boardBitmap), paint);
		this.setImageBitmap(boardBitmap);
	}
}
