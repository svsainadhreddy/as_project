package com.example.myapplicationpopc.model;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "HospitalDB";
    private static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE patients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "doctorId TEXT, " +
                "patientId TEXT, " +
                "name TEXT, " +
                "age INTEGER, " +
                "phone TEXT, " +
                "weight REAL, " +
                "height REAL, " +
                "gender TEXT, " +
                "bmi REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS patients");
        onCreate(db);
    }

    // Insert patient
    public boolean insertPatient(String doctorId, String patientId, String name, int age,
                                 String phone, double weight, double height, String gender, double bmi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("doctorId", doctorId);
        cv.put("patientId", patientId);
        cv.put("name", name);
        cv.put("age", age);
        cv.put("phone", phone);
        cv.put("weight", weight);
        cv.put("height", height);
        cv.put("gender", gender);
        cv.put("bmi", bmi);
        long result = db.insert("patients", null, cv);
        return result != -1;
    }

    // Fetch patients by doctor
    public Cursor getPatientsByDoctor(String doctorId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM patients WHERE doctorId=?", new String[]{doctorId});
    }

    // Search patients by doctor & keyword
    public Cursor searchPatients(String doctorId, String keyword) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM patients WHERE doctorId=? AND (name LIKE ? OR patientId LIKE ?)",
                new String[]{doctorId, "%" + keyword + "%", "%" + keyword + "%"});
    }

    // Update patient
    public boolean updatePatient(int id, String name, int age, String phone,
                                 double weight, double height, String gender, double bmi) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("age", age);
        cv.put("phone", phone);
        cv.put("weight", weight);
        cv.put("height", height);
        cv.put("gender", gender);
        cv.put("bmi", bmi);
        int rows = db.update("patients", cv, "id=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
}
