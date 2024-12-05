package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class AlarmDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "alarms.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ALARMS = "alarms";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_HOUR = "hour";
    private static final String COLUMN_MINUTE = "minute";
    private static final String COLUMN_DAYS = "days";
    private static final String COLUMN_REPEAT_COUNT = "repeat_count";

    public AlarmDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_ALARMS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_HOUR + " INTEGER, " +
                COLUMN_MINUTE + " INTEGER, " +
                COLUMN_DAYS + " TEXT, " +
                COLUMN_REPEAT_COUNT + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }

    public long addAlarm(int hour, int minute, boolean[] days, int repeatCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUR, hour);
        values.put(COLUMN_MINUTE, minute);
        values.put(COLUMN_DAYS, booleanArrayToString(days));
        values.put(COLUMN_REPEAT_COUNT, repeatCount);
        return db.insert(TABLE_ALARMS, null, values);
    }

    public void deleteAlarm(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALARMS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<AlarmData> getAllAlarms() {
        List<AlarmData> alarmList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ALARMS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                AlarmData alarm = new AlarmData(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_HOUR)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_MINUTE)),
                        stringToBooleanArray(cursor.getString(cursor.getColumnIndex(COLUMN_DAYS))),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_REPEAT_COUNT))
                );
                alarmList.add(alarm);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alarmList;
    }

    private String booleanArrayToString(boolean[] array) {
        StringBuilder sb = new StringBuilder();
        for (boolean b : array) {
            sb.append(b ? "1" : "0");
        }
        return sb.toString();
    }

    private boolean[] stringToBooleanArray(String str) {
        boolean[] array = new boolean[7];
        for (int i = 0; i < str.length() && i < 7; i++) {
            array[i] = str.charAt(i) == '1';
        }
        return array;
    }
}