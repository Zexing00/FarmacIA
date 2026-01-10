package com.example.farmacia.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "farmacia.db";
    private static final int DATABASE_VERSION = 5; // Incremented to trigger onUpgrade

    // User Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_IS_ADMIN = "is_admin";

    // Medication Table
    public static final String TABLE_MEDICATIONS = "medications";
    public static final String COLUMN_MED_ID = "id";
    public static final String COLUMN_MED_NAME = "name";
    public static final String COLUMN_MED_LEAFLET = "leaflet";

    // Pillbox Table (User-Medication Relationship)
    public static final String TABLE_PILLBOX = "pillbox";
    public static final String COLUMN_PILLBOX_ID = "id";
    public static final String COLUMN_PILLBOX_USER_ID = "user_id";
    public static final String COLUMN_PILLBOX_MED_ID = "medication_id";
    public static final String COLUMN_PILLBOX_EXPIRY_DATE = "expiry_date";
    public static final String COLUMN_PILLBOX_WEEKLY_DOSE = "weekly_dose";
    
    // Caregivers Table (Patient-Caregiver Relationship)
    public static final String TABLE_CAREGIVERS = "caregivers";
    public static final String COLUMN_CG_ID = "id";
    public static final String COLUMN_CG_PATIENT_ID = "patient_id";
    public static final String COLUMN_CG_CAREGIVER_ID = "caregiver_id";

    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_NAME + " TEXT, " +
            COLUMN_PASSWORD + " TEXT, " +
            COLUMN_IS_ADMIN + " INTEGER DEFAULT 0" +
            ");";

    private static final String TABLE_CREATE_MEDICATIONS =
            "CREATE TABLE " + TABLE_MEDICATIONS + " (" +
            COLUMN_MED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_MED_NAME + " TEXT, " +
            COLUMN_MED_LEAFLET + " TEXT" +
            ");";

    private static final String TABLE_CREATE_PILLBOX =
            "CREATE TABLE " + TABLE_PILLBOX + " (" +
            COLUMN_PILLBOX_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_PILLBOX_USER_ID + " INTEGER, " +
            COLUMN_PILLBOX_MED_ID + " INTEGER, " +
            COLUMN_PILLBOX_EXPIRY_DATE + " TEXT, " +
            COLUMN_PILLBOX_WEEKLY_DOSE + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_PILLBOX_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_PILLBOX_MED_ID + ") REFERENCES " + TABLE_MEDICATIONS + "(" + COLUMN_MED_ID + ")" +
            ");";

    private static final String TABLE_CREATE_CAREGIVERS =
            "CREATE TABLE " + TABLE_CAREGIVERS + " (" +
            COLUMN_CG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_CG_PATIENT_ID + " INTEGER, " +
            COLUMN_CG_CAREGIVER_ID + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_CG_PATIENT_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), " +
            "FOREIGN KEY(" + COLUMN_CG_CAREGIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ")" +
            ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_MEDICATIONS);
        db.execSQL(TABLE_CREATE_PILLBOX);
        db.execSQL(TABLE_CREATE_CAREGIVERS);

        // Initial data - Note: UI text can remain in Spanish
        db.execSQL("INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_NAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_IS_ADMIN + ") VALUES ('admin', 'admin', 1);");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_NAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_IS_ADMIN + ") VALUES ('user', 'user', 0);");
        db.execSQL("INSERT INTO " + TABLE_USERS + " (" + COLUMN_USER_NAME + ", " + COLUMN_PASSWORD + ", " + COLUMN_IS_ADMIN + ") VALUES ('hijo', '1234', 0);");

        db.execSQL("INSERT INTO " + TABLE_MEDICATIONS + " (" + COLUMN_MED_NAME + ", " + COLUMN_MED_LEAFLET + ") VALUES ('Ibuprofeno', 'Tomar cada 8 horas con comida. Antiinflamatorio.');");
        db.execSQL("INSERT INTO " + TABLE_MEDICATIONS + " (" + COLUMN_MED_NAME + ", " + COLUMN_MED_LEAFLET + ") VALUES ('Paracetamol', 'Tomar cada 6-8 horas para el dolor o fiebre.');");
        db.execSQL("INSERT INTO " + TABLE_MEDICATIONS + " (" + COLUMN_MED_NAME + ", " + COLUMN_MED_LEAFLET + ") VALUES ('Amoxicilina', 'Antibiótico. Completar el tratamiento. Tomar cada 12 horas.');");
        
        db.execSQL("INSERT INTO " + TABLE_PILLBOX + " (" + COLUMN_PILLBOX_USER_ID + ", " + COLUMN_PILLBOX_MED_ID + ", " + COLUMN_PILLBOX_EXPIRY_DATE + ", " + COLUMN_PILLBOX_WEEKLY_DOSE + ") VALUES (2, 1, '2025-12-31', '3 veces al día');");
        db.execSQL("INSERT INTO " + TABLE_PILLBOX + " (" + COLUMN_PILLBOX_USER_ID + ", " + COLUMN_PILLBOX_MED_ID + ") VALUES (2, 3);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simplified development approach: drop everything and recreate
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CAREGIVERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PILLBOX);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }
}
