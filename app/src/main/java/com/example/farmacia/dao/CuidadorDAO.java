package com.example.farmacia.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.farmacia.db.DatabaseHelper;
import com.example.farmacia.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class CuidadorDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public CuidadorDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Otorga permiso a un usuario para que sea cuidador del paciente actual.
     */
    public boolean agregarCuidador(int idPaciente, int idCuidador) {
        // Verificar si ya existe la relación
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_CUIDADORES,
                new String[]{DatabaseHelper.COLUMN_CUID_ID},
                DatabaseHelper.COLUMN_CUID_PACIENTE_ID + " = ? AND " + DatabaseHelper.COLUMN_CUID_CUIDADOR_ID + " = ?",
                new String[]{String.valueOf(idPaciente), String.valueOf(idCuidador)},
                null, null, null
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return false; // Ya existe
        }
        if (cursor != null) cursor.close();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_CUID_PACIENTE_ID, idPaciente);
        values.put(DatabaseHelper.COLUMN_CUID_CUIDADOR_ID, idCuidador);

        return database.insert(DatabaseHelper.TABLE_CUIDADORES, null, values) != -1;
    }

    /**
     * Elimina el permiso de un cuidador sobre un paciente.
     */
    public boolean eliminarCuidador(int idPaciente, int idCuidador) {
        return database.delete(DatabaseHelper.TABLE_CUIDADORES,
                DatabaseHelper.COLUMN_CUID_PACIENTE_ID + " = ? AND " + DatabaseHelper.COLUMN_CUID_CUIDADOR_ID + " = ?",
                new String[]{String.valueOf(idPaciente), String.valueOf(idCuidador)}) > 0;
    }

    /**
     * Obtiene la lista de usuarios (pacientes) que han autorizado al usuario actual como cuidador.
     */
    public List<Usuario> obtenerPacientesAsignados(int idCuidador) {
        List<Usuario> pacientes = new ArrayList<>();

        String query = "SELECT u." + DatabaseHelper.COLUMN_ID + ", " +
                       "u." + DatabaseHelper.COLUMN_NOMBRE_USUARIO + " " +
                       "FROM " + DatabaseHelper.TABLE_USUARIOS + " u " +
                       "INNER JOIN " + DatabaseHelper.TABLE_CUIDADORES + " c " +
                       "ON u." + DatabaseHelper.COLUMN_ID + " = c." + DatabaseHelper.COLUMN_CUID_PACIENTE_ID + " " +
                       "WHERE c." + DatabaseHelper.COLUMN_CUID_CUIDADOR_ID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idCuidador)});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nombreIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOMBRE_USUARIO);

            do {
                if (idIndex != -1 && nombreIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String nombre = cursor.getString(nombreIndex);
                    pacientes.add(new Usuario(id, nombre, ""));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return pacientes;
    }

    /**
     * Obtiene la lista de cuidadores que tienen acceso al pastillero del paciente actual.
     */
    public List<Usuario> obtenerCuidadoresPorPaciente(int idPaciente) {
        List<Usuario> cuidadores = new ArrayList<>();

        String query = "SELECT u." + DatabaseHelper.COLUMN_ID + ", " +
                       "u." + DatabaseHelper.COLUMN_NOMBRE_USUARIO + " " +
                       "FROM " + DatabaseHelper.TABLE_USUARIOS + " u " +
                       "INNER JOIN " + DatabaseHelper.TABLE_CUIDADORES + " c " +
                       "ON u." + DatabaseHelper.COLUMN_ID + " = c." + DatabaseHelper.COLUMN_CUID_CUIDADOR_ID + " " +
                       "WHERE c." + DatabaseHelper.COLUMN_CUID_PACIENTE_ID + " = ?";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(idPaciente)});

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nombreIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOMBRE_USUARIO);

            do {
                if (idIndex != -1 && nombreIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String nombre = cursor.getString(nombreIndex);
                    cuidadores.add(new Usuario(id, nombre, ""));
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return cuidadores;
    }
}
