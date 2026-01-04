package com.example.farmacia.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.farmacia.db.DatabaseHelper;
import com.example.farmacia.model.User;

import java.util.ArrayList;
import java.util.List;

public class CaregiverDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public CaregiverDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Grants permission to a user to be a caregiver for the current patient.
     */
    public boolean addCaregiver(int patientId, int caregiverId) {
        // Check if the relationship already exists
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CAREGIVERS,
                new String[]{DatabaseHelper.COLUMN_CG_ID},
                DatabaseHelper.COLUMN_CG_PATIENT_ID + " = ? AND " + DatabaseHelper.COLUMN_CG_CAREGIVER_ID + " = ?",
                new String[]{String.valueOf(patientId), String.valueOf(caregiverId)},
                null, null, null
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return false; // Already exists
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CG_PATIENT_ID, patientId);
        values.put(DatabaseHelper.COLUMN_CG_CAREGIVER_ID, caregiverId);

        return database.insert(DatabaseHelper.TABLE_CAREGIVERS, null, values) != -1;
    }

    /**
     * Removes a caregiver's permission over a patient.
     */
    public boolean removeCaregiver(int patientId, int caregiverId) {
        return database.delete(DatabaseHelper.TABLE_CAREGIVERS,
                DatabaseHelper.COLUMN_CG_PATIENT_ID + " = ? AND " + DatabaseHelper.COLUMN_CG_CAREGIVER_ID + " = ?",
                new String[]{String.valueOf(patientId), String.valueOf(caregiverId)}) > 0;
    }

    /**
     * Gets the list of users (patients) who have authorized the current user as a caregiver.
     */
    public List<User> getAssignedPatients(int caregiverId) {
        List<User> patients = new ArrayList<>();

        String query = "SELECT u." + DatabaseHelper.COLUMN_ID + ", " +
                       "u." + DatabaseHelper.COLUMN_USER_NAME + " " +
                       "FROM " + DatabaseHelper.TABLE_USERS + " u " +
                       "INNER JOIN " + DatabaseHelper.TABLE_CAREGIVERS + " c " +
                       "ON u." + DatabaseHelper.COLUMN_ID + " = c." + DatabaseHelper.COLUMN_CG_PATIENT_ID + " " +
                       "WHERE c." + DatabaseHelper.COLUMN_CG_CAREGIVER_ID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(caregiverId)});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);

            do {
                if (idIndex != -1 && nameIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    patients.add(new User(id, name, "")); // Password is not needed here
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return patients;
    }

    /**
     * Gets the list of caregivers who have access to the current patient's pillbox.
     */
    public List<User> getCaregiversByPatient(int patientId) {
        List<User> caregivers = new ArrayList<>();

        String query = "SELECT u." + DatabaseHelper.COLUMN_ID + ", " +
                       "u." + DatabaseHelper.COLUMN_USER_NAME + " " +
                       "FROM " + DatabaseHelper.TABLE_USERS + " u " +
                       "INNER JOIN " + DatabaseHelper.TABLE_CAREGIVERS + " c " +
                       "ON u." + DatabaseHelper.COLUMN_ID + " = c." + DatabaseHelper.COLUMN_CG_CAREGIVER_ID + " " +
                       "WHERE c." + DatabaseHelper.COLUMN_CG_PATIENT_ID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(patientId)});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME);

            do {
                if (idIndex != -1 && nameIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    caregivers.add(new User(id, name, "")); // Password is not needed here
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return caregivers;
    }
}
