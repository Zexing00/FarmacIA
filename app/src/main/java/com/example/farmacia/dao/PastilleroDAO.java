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
        
        // Consulta SQL con JOIN para obtener los medicamentos de un usuario específico
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
}
