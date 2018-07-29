package com.alkowskey.scashp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "ScaShp.db";
    public static final String TABLE_NAME = "ScaShp_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "BRAND";
    public static final String COL_4 = "SDATE";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table "+TABLE_NAME+"(ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, BRAND TEXT, SDATE DATE)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean insertData(String name, String brand, String DATE)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COL_2, name);
        contentValues.put(COL_3, brand);
        contentValues.put(COL_4, DATE);

        if(sqLiteDatabase.insert(TABLE_NAME, null, contentValues)==-1) {
            return false;
        }else return true;
    }


    public Cursor getAllData(String date) {

        return this.getReadableDatabase().rawQuery("select * from "+TABLE_NAME+" WHERE "+COL_4+" = "+date,null);
    }

    public boolean deleteData(String name, String date)
    {
        String sql = "Select "+COL_1+" from "+TABLE_NAME+" where "+COL_2+" = "+name+" AND "+COL_4+" = "+date+" limit 1";
        Cursor res = this.getReadableDatabase().rawQuery(sql, null);
        String id = null;
        if (res.moveToFirst()){
            id = res.getString(0);
            this.getReadableDatabase().execSQL("Delete from "+TABLE_NAME+" where "+COL_1+" = "+id);
        }
        res.close();
        if(id!=null)return true;
        else return false;
    }

}
