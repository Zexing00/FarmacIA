package com.example.farmacia.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.farmacia.db.DatabaseHelper;
import com.example.farmacia.model.Medicamento;

import java.util.ArrayList;
import java.util.List;

public class PastilleroDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public PastilleroDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public List<Medicamento> obtenerMedicamentosPorUsuario(int usuarioId) {
        List<Medicamento> medicamentos = new ArrayList<>();
        
        String query = "SELECT m." + DatabaseHelper.COLUMN_MED_ID + ", " +
                       "m." + DatabaseHelper.COLUMN_MED_NOMBRE + ", " +
                       "m." + DatabaseHelper.COLUMN_MED_PROSPECTO + " " +
                       "FROM " + DatabaseHelper.TABLE_MEDICAMENTOS + " m " +
                       "INNER JOIN " + DatabaseHelper.TABLE_PASTILLERO + " p " +
                       "ON m." + DatabaseHelper.COLUMN_MED_ID + " = p." + DatabaseHelper.COLUMN_PAST_MED_ID + " " +
                       "WHERE p." + DatabaseHelper.COLUMN_PAST_USER_ID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(usuarioId)});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_ID);
            int nombreIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_NOMBRE);
            int prospectoIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_PROSPECTO);

            do {
                if (idIndex != -1 && nombreIndex != -1 && prospectoIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String nombre = cursor.getString(nombreIndex);
                    String prospecto = cursor.getString(prospectoIndex);
                    medicamentos.add(new Medicamento(id, nombre, prospecto));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return medicamentos;
    }

    /**
     * Añade un medicamento al catálogo local si no existe y lo asigna al usuario.
     * Si el prospecto es nulo, se guardará como cadena vacía.
     */
    public boolean agregarMedicamentoUsuario(int usuarioId, String nombreMedicamento, String prospecto) {
        long medId = -1;

        // 1. Verificar si el medicamento ya existe en la tabla local MEDICAMENTOS
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_MEDICAMENTOS,
                new String[]{DatabaseHelper.COLUMN_MED_ID},
                DatabaseHelper.COLUMN_MED_NOMBRE + " = ?",
                new String[]{nombreMedicamento},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MED_ID);
            if (idIndex != -1) {
                medId = cursor.getLong(idIndex);
            }
            cursor.close();
        }

        // 2. Si no existe, insertarlo
        if (medId == -1) {
            ContentValues medValues = new ContentValues();
            medValues.put(DatabaseHelper.COLUMN_MED_NOMBRE, nombreMedicamento);
            medValues.put(DatabaseHelper.COLUMN_MED_PROSPECTO, prospecto != null ? prospecto : "");
            medId = database.insert(DatabaseHelper.TABLE_MEDICAMENTOS, null, medValues);
        }

        if (medId == -1) {
            return false; // Error al insertar medicamento
        }

        // 3. Verificar si el usuario ya tiene este medicamento en su pastillero
        Cursor checkCursor = database.query(
                DatabaseHelper.TABLE_PASTILLERO,
                new String[]{DatabaseHelper.COLUMN_PAST_ID},
                DatabaseHelper.COLUMN_PAST_USER_ID + " = ? AND " + DatabaseHelper.COLUMN_PAST_MED_ID + " = ?",
                new String[]{String.valueOf(usuarioId), String.valueOf(medId)},
                null, null, null
        );

        boolean yaExiste = false;
        if (checkCursor != null) {
            if (checkCursor.getCount() > 0) {
                yaExiste = true;
            }
            checkCursor.close();
        }

        if (yaExiste) {
            return false; // Ya lo tiene asignado
        }

        // 4. Asignar al usuario en la tabla PASTILLERO
        ContentValues pastilleroValues = new ContentValues();
        pastilleroValues.put(DatabaseHelper.COLUMN_PAST_USER_ID, usuarioId);
        pastilleroValues.put(DatabaseHelper.COLUMN_PAST_MED_ID, medId);
        
        return database.insert(DatabaseHelper.TABLE_PASTILLERO, null, pastilleroValues) != -1;
    }
}
