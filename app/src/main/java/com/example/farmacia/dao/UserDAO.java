package com.example.farmacia.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.farmacia.db.DatabaseHelper;
import com.example.farmacia.model.Administrator;
import com.example.farmacia.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    /**
     * Hashes the password using SHA-256 for better security.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("UserDAO", "Error hashing password", e);
            return password; // Fallback (not recommended in production)
        }
    }

    public User login(String username, String password) {
        // Hash the input password to compare it with the stored one
        String hashedInput = hashPassword(password);

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COLUMN_USER_NAME + " = ? AND " + DatabaseHelper.COLUMN_PASSWORD + " = ?",
                new String[]{username, hashedInput},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);
            int passIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PASSWORD);
            int adminIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_IS_ADMIN);

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
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public long registerUser(String username, String password, boolean isAdmin) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_NAME, username);
        // Save the hashed password
        values.put(DatabaseHelper.COLUMN_PASSWORD, hashPassword(password));
        values.put(DatabaseHelper.COLUMN_IS_ADMIN, isAdmin ? 1 : 0);

        return database.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    public int getUserIdByName(String username) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_USER_NAME + " = ?",
                new String[]{username},
                null, null, null
        );

        int id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            if (idIndex != -1) id = cursor.getInt(idIndex);
            cursor.close();
        }
        return id;
    }
}
