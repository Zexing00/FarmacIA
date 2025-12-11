package com.example.farmacia.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "farmacia.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE_USUARIO = "nombre_usuario";
    public static final String COLUMN_CONTRASENA = "contrasena";
    public static final String COLUMN_ES_ADMIN = "es_admin";

    private static final String TABLE_CREATE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NOMBRE_USUARIO + " TEXT, " +
            COLUMN_CONTRASENA + " TEXT, " +
            COLUMN_ES_ADMIN + " INTEGER DEFAULT 0" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USUARIOS);
        // Insertar un usuario admin por defecto para pruebas
        db.execSQL("INSERT INTO " + TABLE_USUARIOS + " (" + COLUMN_NOMBRE_USUARIO + ", " + COLUMN_CONTRASENA + ", " + COLUMN_ES_ADMIN + ") VALUES ('admin', 'admin', 1);");
        db.execSQL("INSERT INTO " + TABLE_USUARIOS + " (" + COLUMN_NOMBRE_USUARIO + ", " + COLUMN_CONTRASENA + ", " + COLUMN_ES_ADMIN + ") VALUES ('user', 'user', 0);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }
}
