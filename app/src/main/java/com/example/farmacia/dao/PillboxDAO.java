package com.example.farmacia.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.farmacia.db.DatabaseHelper;
import com.example.farmacia.model.Medication;

import java.util.ArrayList;
import java.util.List;

public class PillboxDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public PillboxDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Medication> getMedicationsByUserId(int userId) {
        List<Medication> medications = new ArrayList<>();
        
        String query = "SELECT m." + DatabaseHelper.COLUMN_MED_ID + ", " +
                       "m." + DatabaseHelper.COLUMN_MED_NAME + ", " +
                       "m." + DatabaseHelper.COLUMN_MED_LEAFLET + ", " +
                       "p." + DatabaseHelper.COLUMN_PILLBOX_EXPIRY_DATE + ", " +
                       "p." + DatabaseHelper.COLUMN_PILLBOX_WEEKLY_DOSE + " " +
                       "FROM " + DatabaseHelper.TABLE_MEDICATIONS + " m " +
                       "INNER JOIN " + DatabaseHelper.TABLE_PILLBOX + " p " +
                       "ON m." + DatabaseHelper.COLUMN_MED_ID + " = p." + DatabaseHelper.COLUMN_PILLBOX_MED_ID + " " +
                       "WHERE p." + DatabaseHelper.COLUMN_PILLBOX_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_NAME);
            int leafletIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_LEAFLET);
            int expiryIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PILLBOX_EXPIRY_DATE);
            int doseIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PILLBOX_WEEKLY_DOSE);

            do {
                if (idIndex != -1 && nameIndex != -1 && leafletIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    String leaflet = cursor.getString(leafletIndex);
                    String expiry = (expiryIndex != -1) ? cursor.getString(expiryIndex) : null;
                    String dose = (doseIndex != -1) ? cursor.getString(doseIndex) : null;
                    
                    medications.add(new Medication(id, name, leaflet, expiry, dose));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return medications;
    }

    public boolean addMedicationToUser(int userId, String medicationName, String leaflet) {
        long medId = -1;

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_MEDICATIONS,
                new String[]{DatabaseHelper.COLUMN_MED_ID},
                DatabaseHelper.COLUMN_MED_NAME + " = ?",
                new String[]{medicationName},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_ID);
            if (idIndex != -1) {
                medId = cursor.getLong(idIndex);
            }
            cursor.close();
        }

        if (medId == -1) {
            ContentValues medValues = new ContentValues();
            medValues.put(DatabaseHelper.COLUMN_MED_NAME, medicationName);
            medValues.put(DatabaseHelper.COLUMN_MED_LEAFLET, leaflet != null ? leaflet : "");
            medId = database.insert(DatabaseHelper.TABLE_MEDICATIONS, null, medValues);
        }

        if (medId == -1) {
            return false;
        }

        Cursor checkCursor = database.query(
                DatabaseHelper.TABLE_PILLBOX,
                new String[]{DatabaseHelper.COLUMN_PILLBOX_ID},
                DatabaseHelper.COLUMN_PILLBOX_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_PILLBOX_MED_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(medId)},
                null, null, null
        );

        boolean alreadyExists = false;
        if (checkCursor != null) {
            if (checkCursor.getCount() > 0) {
                alreadyExists = true;
            }
            checkCursor.close();
        }

        if (alreadyExists) {
            return false; // Already in the pillbox
        }

        ContentValues pillboxValues = new ContentValues();
        pillboxValues.put(DatabaseHelper.COLUMN_PILLBOX_USER_ID, userId);
        pillboxValues.put(DatabaseHelper.COLUMN_PILLBOX_MED_ID, medId);
        
        return database.insert(DatabaseHelper.TABLE_PILLBOX, null, pillboxValues) != -1;
    }

    public boolean updatePillbox(int userId, int medicationId, String newExpiryDate, String newDose) {
        ContentValues values = new ContentValues();
        if (newExpiryDate != null) values.put(DatabaseHelper.COLUMN_PILLBOX_EXPIRY_DATE, newExpiryDate);
        if (newDose != null) values.put(DatabaseHelper.COLUMN_PILLBOX_WEEKLY_DOSE, newDose);

        int rows = database.update(
                DatabaseHelper.TABLE_PILLBOX,
                values,
                DatabaseHelper.COLUMN_PILLBOX_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_PILLBOX_MED_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(medicationId)}
        );
        return rows > 0;
    }

    public boolean removeMedicationFromUser(int userId, int medicationId) {
        int rows = database.delete(
                DatabaseHelper.TABLE_PILLBOX,
                DatabaseHelper.COLUMN_PILLBOX_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_PILLBOX_MED_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(medicationId)}
        );
        return rows > 0;
    }
}
