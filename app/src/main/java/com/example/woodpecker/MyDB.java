package com.example.woodpecker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MyDB extends SQLiteOpenHelper {

    public MyDB(@Nullable Context context) {
        super(context, "woodpecker", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE Standards (" +
                        "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "'name' VARCHAR(255)" +
                ");"
        );
        db.execSQL(
                "CREATE TABLE MorseCharacters (" +
                        "'id' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        "'rep' VARCHAR(255) NOT NULL," +
                        "'seq' VARCHAR(255) NOT NULL," +
                        "'standard_id' INTEGER NOT NULL," +
                        "FOREIGN KEY('standard_id') REFERENCES Standards('id') ON DELETE CASCADE" +
                ");"
        );

        // Morse code charset
        String[][] IMC_charset = {
                { "A", ".-" }, { "B", "-..." }, { "C", "-.-." }, { "D", "-.." },
                { "E", "." }, { "F", "..-." }, { "G", "--." }, { "H", "...." },
                { "I", ".." }, { "J", ".---" }, { "K", "-.-" }, { "L", ".-.." },
                { "M", "--" }, { "N", "-." }, { "O", "---" }, { "P", ".--." },
                { "Q", "--.-" }, { "R", ".-." }, { "S", "..." }, { "T", "-" },
                { "U", "..-" }, { "V", "...-" }, { "W", ".--" }, { "X", "-..-" },
                { "Y", "-.--" }, { "Z", "--.." },
        };
        String[][] CMC_charset = {
                { "A", ".-" }, { "B", "-..." }, { "C", "-.." }, { "D", "-.." },
                { "E", "." }, { "F", "..-." }, { "G", "--." }, { "H", "...." },
                { "I", ".." }, { "J", ".." }, { "K", "-.-" }, { "L", ".-.." },
                { "M", "--" }, { "N", "-." }, { "O", ".-..." }, { "P", "....." },
                { "Q", "--.-" }, { "R", ".-." }, { "S", "..." }, { "T", "-" },
                { "U", "..-" }, { "V", "...-" }, { "W", ".--" }, { "X", "..-..." },
                { "Y", "--..." }, { "Z", ".--.." },

                { "CH", "----" }, { "Ö", "---." }, { "Ü", "..--" },
        };
        insertCharset(db, "International Morse Code", IMC_charset);
        insertCharset(db, "Continental Morse Code", CMC_charset);
    }

    private static void insertCharset(SQLiteDatabase db, String name, String[][] charset) {
        db.execSQL("INSERT INTO Standards (name) VALUES ('" + name + "');");

        int standardId = 1; // Default to 1 in case query fails
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("SELECT last_insert_rowid();", null);
            if (cursor.moveToFirst()) {
                standardId = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        String sql = "INSERT INTO MorseCharacters (rep, seq, standard_id) VALUES (?, ?, ?)";
        SQLiteStatement stmt = db.compileStatement(sql);

        for (String[] entry : charset) {
            stmt.bindString(1, entry[0]); // char
            stmt.bindString(2, entry[1]); // Morse rep
            stmt.bindLong(3, standardId); // standard_id
            stmt.executeInsert();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public void saveMorseCharset(String standard, ArrayList<MorseCharSet.MorseChar> mcs) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Fetch the standard ID
        Cursor standardCursor = db.rawQuery("SELECT id FROM Standards WHERE name = ?", new String[]{standard});
        int standardId = -1;
        try {
            if (standardCursor.moveToFirst()) {
                standardId = standardCursor.getInt(0);
            }
        } finally {
            if (standardCursor != null) standardCursor.close();
        }

        if (standardId != -1) {
            // Remove old Morse characters for the standard
            db.delete("MorseCharacters", "standard_id = ?", new String[]{String.valueOf(standardId)});

            // Insert new Morse characters into the database
            String sql = "INSERT INTO MorseCharacters (rep, seq, standard_id) VALUES (?, ?, ?)";
            SQLiteStatement stmt = db.compileStatement(sql);

            for (MorseCharSet.MorseChar morseChar : mcs) {
                stmt.bindString(1, morseChar.rep);
                stmt.bindString(2, morseChar.sequence);
                stmt.bindLong(3, standardId);
                stmt.executeInsert();
            }
        }
    }

    public MorseCharSet loadMorseCharset(String standard) {
        SQLiteDatabase db = this.getReadableDatabase();
        MorseCharSet morseCharSet = new MorseCharSet(standard);

        // Fetch the standard ID
        Cursor standardCursor = db.rawQuery("SELECT id FROM Standards WHERE name = ?", new String[]{standard});
        int standardId = -1;
        try {
            if (standardCursor.moveToFirst()) {
                standardId = standardCursor.getInt(0);
            }
        } finally {
            if (standardCursor != null) standardCursor.close();
        }

        // If the standard was not found, return an empty MorseCharSet
        if (standardId == -1) {
            db.close();
            return morseCharSet;
        }

        // Fetch Morse characters for the given standard ID
        Cursor cursor = db.rawQuery("SELECT rep, seq FROM MorseCharacters WHERE standard_id = ?",
                new String[]{String.valueOf(standardId)});
        try {
            if (cursor.moveToFirst()) {
                do {
                    String character = cursor.getString(0);
                    String sequence = cursor.getString(1);
                    // System.out.println(": " + character + sequence);
                    morseCharSet.add(character, sequence);
                } while (cursor.moveToNext());
            }
        } finally {
            if ( cursor != null ) cursor.close();
        }

        db.close();
        return morseCharSet;
    }

    public ArrayList<String> getAllMorseCharsets() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id, name FROM Standards", null);

        ArrayList<String> standardNames = new ArrayList<>();

        while (cursor.moveToNext()) {
            standardNames.add(cursor.getString(1));
        }
        cursor.close();
        db.close();

        return standardNames;
    }

    public boolean createMorseCharset(String newStandard) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM Standards WHERE name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{newStandard});

        if (cursor != null && cursor.moveToFirst()) {
            // If a standard with the same name already exists, return false
            cursor.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("name", newStandard);

        long result = db.insert("Standards", null, values);
        return result != -1;
    }
    public boolean deleteMorseCharset(String standard) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Fetch the standard ID
        Cursor standardCursor = db.rawQuery("SELECT id FROM Standards WHERE name = ?", new String[]{standard});
        int standardId = -1;
        try {
            if (standardCursor.moveToFirst()) {
                standardId = standardCursor.getInt(0);
            }
        } finally {
            if (standardCursor != null) standardCursor.close();
        }

        // If the standard was found, delete its corresponding characters and the standard itself
        if (standardId != -1) {
            // Delete Morse characters associated with this standard
            db.delete("MorseCharacters", "standard_id = ?", new String[]{String.valueOf(standardId)});
            // Optionally delete the standard itself
            db.delete("Standards", "id = ?", new String[]{String.valueOf(standardId)});
            return true;
        }
        return false;
    }
}
