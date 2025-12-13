package com.example.farmacia.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "farmacia.db";
    private static final int DATABASE_VERSION = 3; // Incrementamos versión a 3

    // Tabla Usuarios
    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE_USUARIO = "nombre_usuario";
    public static final String COLUMN_CONTRASENA = "contrasena";
    public static final String COLUMN_ES_ADMIN = "es_admin";

    // Tabla Medicamentos
    public static final String TABLE_MEDICAMENTOS = "medicamentos";
    public static final String COLUMN_MED_ID = "id";
    public static final String COLUMN_MED_NOMBRE = "nombre";
    public static final String COLUMN_MED_PROSPECTO = "prospecto";

    // Tabla Pastillero (Relación Usuario - Medicamento)
    public static final String TABLE_PASTILLERO = "pastillero";
    public static final String COLUMN_PAST_ID = "id";
    public static final String COLUMN_PAST_USER_ID = "usuario_id";
    public static final String COLUMN_PAST_MED_ID = "medicamento_id";
    public static final String COLUMN_PAST_CADUCIDAD = "fecha_caducidad"; // Nuevo
    public static final String COLUMN_PAST_DOSIS = "dosis_semanal";       // Nuevo

    private static final String TABLE_CREATE_USUARIOS =
            "CREATE TABLE " + TABLE_USUARIOS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NOMBRE_USUARIO + " TEXT, " +
            COLUMN_CONTRASENA + " TEXT, " +
            COLUMN_ES_ADMIN + " INTEGER DEFAULT 0" +
            ");";

    private static final String TABLE_CREATE_MEDICAMENTOS =
            "CREATE TABLE " + TABLE_MEDICAMENTOS + " (" +
            COLUMN_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MED_NOMBRE + " TEXT, " +
            COLUMN_MED_PROSPECTO + " TEXT" +
            ");";

    private static final String TABLE_CREATE_PASTILLERO =
            "CREATE TABLE " + TABLE_PASTILLERO + " (" +
            COLUMN_PAST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PAST_USER_ID + " INTEGER, " +
            COLUMN_PAST_MED_ID + " INTEGER, " +
            COLUMN_PAST_CADUCIDAD + " TEXT, " +
            COLUMN_PAST_DOSIS + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_PAST_USER_ID + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_PAST_MED_ID + ") REFERENCES " + TABLE_MEDICAMENTOS + "(" + COLUMN_MED_ID + ")" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USUARIOS);
        db.execSQL(TABLE_CREATE_MEDICAMENTOS);
        db.execSQL(TABLE_CREATE_PASTILLERO);

        // Datos iniciales
        db.execSQL("INSERT INTO " + TABLE_USUARIOS + " (" + COLUMN_NOMBRE_USUARIO + ", " + COLUMN_CONTRASENA + ", " + COLUMN_ES_ADMIN + ") VALUES ('admin', 'admin', 1);");
        db.execSQL("INSERT INTO " + TABLE_USUARIOS + " (" + COLUMN_NOMBRE_USUARIO + ", " + COLUMN_CONTRASENA + ", " + COLUMN_ES_ADMIN + ") VALUES ('user', 'user', 0);");

        db.execSQL("INSERT INTO " + TABLE_MEDICAMENTOS + " (" + COLUMN_MED_NOMBRE + ", " + COLUMN_MED_PROSPECTO + ") VALUES ('Ibuprofeno', 'Tomar cada 8 horas con comida. Antiinflamatorio.');");
        db.execSQL("INSERT INTO " + TABLE_MEDICAMENTOS + " (" + COLUMN_MED_NOMBRE + ", " + COLUMN_MED_PROSPECTO + ") VALUES ('Paracetamol', 'Tomar cada 6-8 horas para el dolor o fiebre.');");
        db.execSQL("INSERT INTO " + TABLE_MEDICAMENTOS + " (" + COLUMN_MED_NOMBRE + ", " + COLUMN_MED_PROSPECTO + ") VALUES ('Amoxicilina', 'Antibiótico. Completar el tratamiento. Tomar cada 12 horas.');");
        
        // Asignar medicamentos al usuario por defecto
        db.execSQL("INSERT INTO " + TABLE_PASTILLERO + " (" + COLUMN_PAST_USER_ID + ", " + COLUMN_PAST_MED_ID + ", " + COLUMN_PAST_CADUCIDAD + ", " + COLUMN_PAST_DOSIS + ") VALUES (2, 1, '2025-12-31', '3 veces al día');");
        db.execSQL("INSERT INTO " + TABLE_PASTILLERO + " (" + COLUMN_PAST_USER_ID + ", " + COLUMN_PAST_MED_ID + ") VALUES (2, 3);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // En un entorno de producción haríamos una migración ALTER TABLE
        // Pero aquí para simplificar, borramos y recreamos
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PASTILLERO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAMENTOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS);
        onCreate(db);
    }
}
