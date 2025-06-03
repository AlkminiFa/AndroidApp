
package com.example.brokenomore;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TransactionDatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "transactions.db";
    public static final int DATABASE_VERSION = 1;

    public TransactionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Δημιουργία πίνακα συναλλαγών
        db.execSQL("CREATE TABLE Transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "amount REAL," +
                "type TEXT," +
                "category TEXT," +
                "description TEXT," +
                "date TEXT," +
                "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Αν χρειαστεί στο μέλλον
        db.execSQL("DROP TABLE IF EXISTS Transactions");
        onCreate(db);
    }

    // Εισαγωγή νέας συναλλαγής
    public boolean insertTransaction(int userId, double amount, String type, String category, String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("amount", amount);
        values.put("type", type);
        values.put("category", category);
        values.put("description", description);
        values.put("date", date);

        long result = db.insert("Transactions", null, values);
        return result != -1;
    }

    // Επιστροφή όλων των συναλλαγών για συγκεκριμένο χρήστη
    public Cursor getTransactionsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Transactions WHERE user_id = ? ORDER BY date DESC", new String[]{String.valueOf(userId)});
    }

    // Διαγραφή συναλλαγών που είναι παλαιότερες από 2 έτοι
    public void deleteOldTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String oneYearAgo = sdf.format(calendar.getTime());

        db.delete("Transactions", "date < ?", new String[]{oneYearAgo});
    }

    // Επιστρέφει το σύνολο εξόδων χρήστη για συγκεκριμένη κατηγορία
    public float getTotalSpentByCategory(int userId, String category) {
        float total = 0f;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM Transactions WHERE user_id = ? AND category = ? AND type = 'expense'",
                new String[]{String.valueOf(userId), category}
        );
        if (cursor.moveToFirst()) {
            total = cursor.isNull(0) ? 0f : cursor.getFloat(0);
        }
        cursor.close();
        return total;
    }
}
