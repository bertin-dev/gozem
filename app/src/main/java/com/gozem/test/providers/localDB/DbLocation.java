package com.gozem.test.providers.localDB;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;
import com.gozem.test.model.User;

import java.util.ArrayList;
import java.util.List;

public class DbLocation extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "location_db";
    private static final String TABLE_LOCATION = "location";
    private static final String KEY_ID = "id";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LOGITUDE = "logitude";


    public DbLocation(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOCATION + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_LATITUDE + " TEXT,"
                + KEY_LOGITUDE + " TEXT " + ")";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION);
        // Create tables again
        onCreate(db);
    }
    // **** CRUD (Create, Read, Update, Delete) Operations ***** //



    public void AddLnLg(double latitude, double longitude){
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_LATITUDE, latitude);
        cValues.put(KEY_LOGITUDE, longitude);
        // Insert the new row, returning the primary key value of the new row
        db.insert(TABLE_LOCATION,null, cValues);
        db.close();
    }



    @SuppressLint("Range")
    public List<LatLng> getAllLocations() {
        // array of columns to fetch
        String[] columns = {
                KEY_ID,
                KEY_LATITUDE,
                KEY_LOGITUDE
        };
        // sorting orders
        String sortOrder =
                KEY_ID + " ASC";
        List<LatLng> latLngArrayList = new ArrayList<LatLng>();
        SQLiteDatabase db = this.getReadableDatabase();
        // query the user table
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id,user_name,user_email,user_password FROM user ORDER BY user_name;
         */
        Cursor cursor = db.query(TABLE_LOCATION, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        // Traversing through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LatLng latLng = new LatLng(cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)), cursor.getDouble(cursor.getColumnIndex(KEY_LOGITUDE)));
                latLngArrayList.add(latLng);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return user list
        return latLngArrayList;
    }


    public void clearLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete user record
        db.delete(TABLE_LOCATION, null, null);
        db.close();
    }

}

