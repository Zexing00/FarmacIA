package com.example.farmacia.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.example.farmacia.db.DatabaseHelper;
import com.example.farmacia.model.Administrador;
import com.example.farmacia.model.Usuario;

public class UsuarioDAO {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public UsuarioDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Usuario login(String username, String password) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USUARIOS,
                null,
                DatabaseHelper.COLUMN_NOMBRE_USUARIO + " = ? AND " + DatabaseHelper.COLUMN_CONTRASENA + " = ?",
                new String[]{username, password},
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);
            int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_NOMBRE_USUARIO);
            int passIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CONTRASENA);
            int adminIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ES_ADMIN);

            // Verificar si las columnas existen
            if (idIndex != -1 && nameIndex != -1 && passIndex != -1 && adminIndex != -1) {
                int id = cursor.getInt(idIndex);
                String nombre = cursor.getString(nameIndex);
                String pass = cursor.getString(passIndex);
                int esAdmin = cursor.getInt(adminIndex);

                cursor.close();

                if (esAdmin == 1) {
                    return new Administrador(id, nombre, pass);
                } else {
                    return new Usuario(id, nombre, pass);
                }
            }
            cursor.close();
        }
        return null;
    }

    public boolean existeUsuario(String username) {
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_USUARIOS,
                new String[]{DatabaseHelper.COLUMN_ID},
                DatabaseHelper.COLUMN_NOMBRE_USUARIO + " = ?",
                new String[]{username},
                null,
                null,
                null
        );
        
        boolean existe = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return existe;
    }

    public long registrarUsuario(String nombre, String contrasena, boolean esAdmin) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOMBRE_USUARIO, nombre);
        values.put(DatabaseHelper.COLUMN_CONTRASENA, contrasena);
        values.put(DatabaseHelper.COLUMN_ES_ADMIN, esAdmin ? 1 : 0);

        return database.insert(DatabaseHelper.TABLE_USUARIOS, null, values);
    }
}
