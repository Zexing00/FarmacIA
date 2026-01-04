package com.example.farmacia.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.farmacia.db.DatabaseHelper;
import com.example.farmacia.model.Administrator;
import com.example.farmacia.model.User;

public class UserDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public User login(String username, String password) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_NAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?",
                new String[]{username, password},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
            int passIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD);
            int adminIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_ADMIN);

            // Check if columns exist
            if (idIndex != -1 && nameIndex != -1 && passIndex != -1 && adminIndex != -1) {
                int id = cursor.getInt(idIndex);
                String name = cursor.getString(nameIndex);
                String pass = cursor.getString(passIndex);
                int isAdmin = cursor.getInt(adminIndex);

                cursor.close();

                if (isAdmin == 1) {
                    return new Administrator(id, name, pass);
                } else {
                    return new User(id, name, pass);
                }
            }
            cursor.close();
        }
        return null;
    }

    public boolean userExists(String username) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_USER_NAME + " = ?",
                new String[]{username},
                null,
                null,
                null
        );
        
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public long registerUser(String name, String password, boolean isAdmin) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, name);
        values.put(DatabaseHelper.COLUMN_PASSWORD, password);
        values.put(DatabaseHelper.COLUMN_IS_ADMIN, isAdmin ? 1 : 0);

        return database.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public int getUserIdByName(String name) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_USER_NAME + " = ?",
                new String[]{name},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            if (idIndex != -1) {
                int id = cursor.getInt(idIndex);
                cursor.close();
                return id;
            }
            cursor.close();
        }
        return -1; // Not found
    }
}
