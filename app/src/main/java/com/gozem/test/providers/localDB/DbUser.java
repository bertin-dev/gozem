package com.gozem.test.providers.localDB;


import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gozem.test.model.User;

import java.util.ArrayList;
import java.util.List;

public class DbUser extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "gozem_db";
    private static final String TABLE_USER = "user";
    private static final String KEY_ID = "id";
    private static final String KEY_FULLNAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_DATE = "created_at";


    public DbUser(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USER + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_FULLNAME + " TEXT," + KEY_EMAIL + " TEXT," + KEY_PASSWORD+ " TEXT,"
                + KEY_DATE + " TEXT " + ")";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        // Create tables again
        onCreate(db);
    }
    // **** CRUD (Create, Read, Update, Delete) Operations ***** //


    /**
     * This method add user
     *
     * @param user
     */
    public void AddUser(User user){
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_FULLNAME, user.getFullName());
        cValues.put(KEY_EMAIL, user.getEmail());
        cValues.put(KEY_PASSWORD, user.getPassword());
        cValues.put(KEY_DATE, user.getCreated_at());
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_USER,null, cValues);
        db.close();
    }

    /**
     * This method to check user exist or not
     *
     * @param email
     * @return true/false
     */
    public boolean checkEmailUser(String email) {
        // array of columns to fetch
        String[] columns = {
                KEY_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = KEY_EMAIL + " = ?";
        // selection argument
        String[] selectionArgs = {email};
        // query user table with condition
        /**
         * Here query function is used to fetch records from user table this function works like we use sql query.
         * SQL query equivalent to this query function is
         * SELECT user_id FROM user WHERE user_email = 'jack@androidtutorialshub.com';
         */
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,                       //group the rows
                null,                      //filter by row groups
                null);                      //The sort order
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        if (cursorCount > 0) {
            return true;
        }
        return false;
    }



    /**
     * This method to check user exist or not
     *
     * @param email
     * @param password
     * @return true/false
     */
    public boolean checkUser(String email, String password) {
        // array of columns to fetch
        String[] columns = {
                KEY_ID
        };
        SQLiteDatabase db = this.getReadableDatabase();
        // selection criteria
        String selection = KEY_EMAIL + " = ?" + " AND " + KEY_PASSWORD + " = ?";
        // selection arguments
        String[] selectionArgs = {email, password};
        // query user table with conditions
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,                    //columns to return
                selection,                  //columns for the WHERE clause
                selectionArgs,              //The values for the WHERE clause
                null,                       //group the rows
                null,                       //filter by row groups
                null);                      //The sort order
        int cursorCount = cursor.getCount();
        cursor.close();
        db.close();
        if (cursorCount > 0) {
            return true;
        }
        return false;
    }









    /**
     * This method is to fetch all user and return the list of user records
     *
     * @return list
     */
    @SuppressLint("Range")
    public List<User> getAllUser() {
        // array of columns to fetch
        String[] columns = {
                KEY_ID,
                KEY_FULLNAME,
                KEY_EMAIL,
                KEY_PASSWORD
        };
        // sorting orders
        String sortOrder =
                KEY_FULLNAME + " ASC";
        List<User> userList = new ArrayList<User>();
        SQLiteDatabase db = this.getReadableDatabase();
        // query the user table
        Cursor cursor = db.query(TABLE_USER, //Table to query
                columns,    //columns to return
                null,        //columns for the WHERE clause
                null,        //The values for the WHERE clause
                null,       //group the rows
                null,       //filter by row groups
                sortOrder); //The sort order
        // Traversing through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                User user = new User();
                user.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(KEY_ID))));
                user.setFullName(cursor.getString(cursor.getColumnIndex(KEY_FULLNAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndex(KEY_EMAIL)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(KEY_PASSWORD)));
                user.setCreated_at(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                // Adding user record to list
                userList.add(user);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // return user list
        return userList;
    }
    /**
     * This method to update user record
     *
     * @param user
     */
    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_FULLNAME, user.getFullName());
        values.put(KEY_EMAIL, user.getEmail());
        values.put(KEY_PASSWORD, user.getPassword());
        values.put(KEY_DATE, user.getCreated_at());
        // updating row
        db.update(TABLE_USER, values, KEY_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }
    /**
     * This method is to delete user record
     *
     * @param user
     */
    public void deleteUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        // delete user record by id
        db.delete(TABLE_USER, KEY_ID + " = ?",
                new String[]{String.valueOf(user.getId())});
        db.close();
    }

}

