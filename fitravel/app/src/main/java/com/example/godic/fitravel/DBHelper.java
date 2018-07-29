package com.example.godic.fitravel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by godic on 2018-07-28.
 */

public class DBHelper extends SQLiteOpenHelper {
    SQLiteDatabase mDB;
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        mDB = db;
        String sql = "create table if not exists place("
                + "idx integer primary key autoincrement, "
                + "name text, "
                + "address text, "
                + "latitude real, "
                + "longitude real,"
                + "state integer);";
        mDB.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String sql = "drop table if exists place";
        mDB.execSQL(sql);
        onCreate(mDB);
    }
}
