package com.example.a2048;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DB_NAME    = "2048_stats.db";
    private static final int    DB_VERSION = 2;

    private static final String TABLE_STATS       = "stats";
    private static final String COL_ID            = "id";
    private static final String COL_HIGHEST_SCORE = "highest_score";
    private static final String COL_TOTAL_SCORE   = "total_score";
    private static final String COL_TOTAL_GAMES   = "total_games";

    // 512 milestone
    private static final String COL_GAMES_512         = "games_512";
    private static final String COL_SHORTEST_TIME_512  = "shortest_time_512"; // stored in seconds
    private static final String COL_FEWEST_MOVES_512   = "fewest_moves_512";

    // 1024 milestone
    private static final String COL_GAMES_1024         = "games_1024";
    private static final String COL_SHORTEST_TIME_1024 = "shortest_time_1024";
    private static final String COL_FEWEST_MOVES_1024  = "fewest_moves_1024";

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_STATS + " ("
                + COL_ID             + " INTEGER PRIMARY KEY, "
                + COL_HIGHEST_SCORE  + " INTEGER DEFAULT 0, "
                + COL_TOTAL_SCORE    + " INTEGER DEFAULT 0, "
                + COL_TOTAL_GAMES    + " INTEGER DEFAULT 0, "
                + COL_GAMES_512          + " INTEGER DEFAULT 0, "
                + COL_SHORTEST_TIME_512  + " INTEGER DEFAULT 0, "
                + COL_FEWEST_MOVES_512   + " INTEGER DEFAULT 0, "
                + COL_GAMES_1024         + " INTEGER DEFAULT 0, "
                + COL_SHORTEST_TIME_1024 + " INTEGER DEFAULT 0, "
                + COL_FEWEST_MOVES_1024  + " INTEGER DEFAULT 0)";
        db.execSQL(createTable);
        db.execSQL("INSERT INTO " + TABLE_STATS + " VALUES (1,0,0,0,0,0,0,0,0,0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
        onCreate(db);
    }

    // ---- Read ----

    public int getHighestScore()      { return getStat(COL_HIGHEST_SCORE); }
    public int getTotalScore()        { return getStat(COL_TOTAL_SCORE);   }
    public int getTotalGames()        { return getStat(COL_TOTAL_GAMES);   }
    public int getGames512()          { return getStat(COL_GAMES_512);         }
    public int getShortestTime512()   { return getStat(COL_SHORTEST_TIME_512); }
    public int getFewestMoves512()    { return getStat(COL_FEWEST_MOVES_512);  }
    public int getGames1024()         { return getStat(COL_GAMES_1024);        }
    public int getShortestTime1024()  { return getStat(COL_SHORTEST_TIME_1024);}
    public int getFewestMoves1024()   { return getStat(COL_FEWEST_MOVES_1024); }

    private int getStat(String col) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STATS, new String[]{col},
                COL_ID + "=1", null, null, null, null);
        int val = 0;
        if (cursor.moveToFirst()) val = cursor.getInt(0);
        cursor.close();
        return val;
    }

    // ---- Write ----

    // reached512/1024: whether the player hit those tiles this game
    // time is in seconds
    public void saveGameResult(int score, int moves, int timeSeconds,
                               boolean reached512, boolean reached1024) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COL_TOTAL_SCORE, getTotalScore() + score);
        values.put(COL_TOTAL_GAMES, getTotalGames() + 1);

        if (score > getHighestScore())
            values.put(COL_HIGHEST_SCORE, score);

        if (reached512) {
            values.put(COL_GAMES_512, getGames512() + 1);
            int best512Time  = getShortestTime512();
            int best512Moves = getFewestMoves512();
            if (best512Time == 0 || timeSeconds < best512Time)
                values.put(COL_SHORTEST_TIME_512, timeSeconds);
            if (best512Moves == 0 || moves < best512Moves)
                values.put(COL_FEWEST_MOVES_512, moves);
        }

        if (reached1024) {
            values.put(COL_GAMES_1024, getGames1024() + 1);
            int best1024Time  = getShortestTime1024();
            int best1024Moves = getFewestMoves1024();
            if (best1024Time == 0 || timeSeconds < best1024Time)
                values.put(COL_SHORTEST_TIME_1024, timeSeconds);
            if (best1024Moves == 0 || moves < best1024Moves)
                values.put(COL_FEWEST_MOVES_1024, moves);
        }

        db.update(TABLE_STATS, values, COL_ID + "=1", null);
    }

    // Helper to format seconds → "m:ss"
    public static String formatTime(int totalSeconds) {
        if (totalSeconds == 0) return "—";
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }
}