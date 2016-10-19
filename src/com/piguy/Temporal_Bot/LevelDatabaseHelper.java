package com.piguy.Temporal_Bot;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Manager for the level database and score database
 *
 * @author Alex Vanyo
 */
public class LevelDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "levels";
    private static final String TABLE_LEVELS = "levels";

    private static final String KEY_ID = "_id";
    private static final String KEY_LEVEL_STRING = "level_string";
    private static final String KEY_NAME = "name";
    private static final String KEY_TARGET_TIME = "target_time";
    private static final String KEY_TARGET_ROBOTS = "target_robots";
    private static final String KEY_INSTRUCTION = "instruction";

    private final Context context;

    public LevelDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        this.context = context;
    }

    /**
     * Copies the database from the packaged database to the internal database
     *
     * @param path path to copy to
     * @throws IOException
     */
    private void copyDataBase(String path) throws IOException {

        InputStream myInput = context.getAssets().open(DATABASE_NAME);
        OutputStream myOutput = new FileOutputStream(path);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Always drops and loads the level database from the packaged one
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LEVELS + ";");
        db.execSQL("CREATE TABLE " + TABLE_LEVELS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_LEVEL_STRING + " TEXT, " + KEY_NAME + " TEXT, " + KEY_TARGET_TIME + " INTEGER, " + KEY_TARGET_ROBOTS + " INTEGER, " + KEY_INSTRUCTION + "TEXT);");

        try {
            copyDataBase(db.getPath());
        } catch (IOException e) {
            throw new Error("Unable to create database");
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    /**
     * Updates the score for the passed level
     *
     * @param level level that needs a score to be updated
     */
    public void updateScore(Level level) {
        new ScoresDatabaseHelper(context).updateScore(level.getScore());
    }

    /**
     * Gets the level for the passed id
     *
     * @param id id for the wanted level
     * @return level with that id
     */
    public Level getLevel(int id) {

        Cursor levelCursor = this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_LEVELS + " WHERE " + KEY_ID + " = '" + id + "';", null);

        Score score = new ScoresDatabaseHelper(context).getScore(id);

        Level returnLevel = null;

        if (levelCursor != null && levelCursor.moveToFirst()) {
            returnLevel = new Level(levelCursor.getInt(0), levelCursor.getString(1), levelCursor.getString(2), levelCursor.getLong(3), levelCursor.getInt(4), levelCursor.getString(5), score);

            levelCursor.close();
        }

        this.getReadableDatabase().close();

        return returnLevel;
    }

    /**
     * Gets all levels from the database
     *
     * @return all levels
     */
    public ArrayList<Level> getAllLevels() {
        ArrayList<Level> levelList = new ArrayList<Level>();

        Cursor levelCursor = this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_LEVELS + ";", null);
        ScoresDatabaseHelper scoresHelper = new ScoresDatabaseHelper(context);

        if (levelCursor.moveToFirst()) {
            do {
                Level level = new Level(levelCursor.getInt(0), levelCursor.getString(1), levelCursor.getString(2), levelCursor.getLong(3), levelCursor.getInt(4), levelCursor.getString(5), scoresHelper.getScore(levelCursor.getInt(0)));

                levelList.add(level);
            } while (levelCursor.moveToNext());
        }

        this.getReadableDatabase().close();
        levelCursor.close();

        return levelList;
    }

    /**
     * Manager for the score database
     */
    private class ScoresDatabaseHelper extends SQLiteOpenHelper {

        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_NAME = "scores";
        private static final String TABLE_SCORES = "scores";

        private static final String KEY_ID = "id";
        private static final String KEY_COMPLETED = "completed";
        private static final String KEY_TIME = "time";
        private static final String KEY_ROBOTS = "robots";

        public ScoresDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + TABLE_SCORES + "(" + KEY_ID + " INTEGER, " + KEY_COMPLETED + " INTEGER, " + KEY_TIME + " INTEGER, " + KEY_ROBOTS + " INTEGER);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        /**
         * Updates a score by either modifying the current one or adding a new one
         *
         * @param score new score that will be put into the database
         */
        public void updateScore(Score score) {
            if (getScore(score.getID()).getCompleted()) {
                this.getWritableDatabase().execSQL("UPDATE " + TABLE_SCORES + " SET " + KEY_COMPLETED + "='" + (score.getCompleted() ? 1 : 0) + "', " + KEY_TIME + "='" + score.getTime() + "', " + KEY_ROBOTS + "='" + score.getRobots() + "' WHERE " + KEY_ID + "='" + score.getID() + "';");
            } else {
                this.getWritableDatabase().execSQL("INSERT INTO " + TABLE_SCORES + " VALUES(" + score.getID() + ", " + (score.getCompleted() ? 1 : 0) + ", " + score.getTime() + ", " + score.getRobots() + ");");

            }

            this.getWritableDatabase().close();
        }

        /**
         * Gets the score with the passed id, or the default score if there is no score
         *
         * @param id id for the wanted score
         * @return score for the given id
         */
        public Score getScore(int id) {
            Score returnScore = new Score(id);

            Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_SCORES + " WHERE " + KEY_ID + " = '" + id + "';", null);

            if (cursor != null && cursor.moveToFirst()) {
                returnScore = new Score(cursor.getInt(0), cursor.getInt(1) == 1, cursor.getLong(2), cursor.getInt(3));

                cursor.close();
            }

            this.getReadableDatabase().close();
            return returnScore;
        }
    }

    private static final String LOG_TAG = "LevelDatabaseHelper";
}
