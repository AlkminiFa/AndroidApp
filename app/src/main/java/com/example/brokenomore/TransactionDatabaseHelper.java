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
    public static final int DATABASE_VERSION = 2;

    public TransactionDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Πίνακας συναλλαγών
        db.execSQL("CREATE TABLE Transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "amount REAL," +
                "type TEXT," +
                "category TEXT," +
                "description TEXT," +
                "date TEXT," +
                "FOREIGN KEY(user_id) REFERENCES Users(id) ON DELETE CASCADE)");

        // Νέος πίνακας budget/ημερών
        db.execSQL("CREATE TABLE UserBudgetData (" +
                "user_id INTEGER PRIMARY KEY," +
                "budget REAL," +
                "initialBudget REAL," +
                "daysLeft INTEGER," +
                "lastOpenedDate TEXT," +
                "FOREIGN KEY(user_id) REFERENCES Users(id))");
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS Transactions");
        db.execSQL("DROP TABLE IF EXISTS UserBudgetData");
        onCreate(db);
    }

    // Εισαγωγή συναλλαγής
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

    // Ανάκτηση συναλλαγών
    public Cursor getTransactionsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM Transactions WHERE user_id = ? ORDER BY date DESC", new String[]{String.valueOf(userId)});
    }

    // Διαγραφή παλαιών συναλλαγών
    public void deleteOldTransactions() {
        SQLiteDatabase db = this.getWritableDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -2);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String oneYearAgo = sdf.format(calendar.getTime());

        db.delete("Transactions", "date < ?", new String[]{oneYearAgo});
    }

    // Σύνολο εξόδων ανά κατηγορία
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

    // ➕ Αποθήκευση / ενημέρωση budget info
    public void saveOrUpdateUserBudget(int userId, float budget, float initialBudget, int daysLeft, String lastOpenedDate) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT user_id FROM UserBudgetData WHERE user_id = ?", new String[]{String.valueOf(userId)});
        boolean exists = cursor.moveToFirst();
        cursor.close();

        ContentValues values = new ContentValues();
        values.put("budget", budget);
        values.put("initialBudget", initialBudget);
        values.put("daysLeft", daysLeft);
        values.put("lastOpenedDate", lastOpenedDate);
        values.put("user_id", userId);

        if (exists) {
            db.update("UserBudgetData", values, "user_id = ?", new String[]{String.valueOf(userId)});
        } else {
            db.insert("UserBudgetData", null, values);
        }
    }

    // ➕ Ανάγνωση τιμών
    public float getBudget(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT budget FROM UserBudgetData WHERE user_id = ?", new String[]{String.valueOf(userId)});
        float result = 0f;
        if (cursor.moveToFirst()) result = cursor.getFloat(0);
        cursor.close();
        return result;
    }

    public float getInitialBudget(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT initialBudget FROM UserBudgetData WHERE user_id = ?", new String[]{String.valueOf(userId)});
        float result = 0f;
        if (cursor.moveToFirst()) result = cursor.getFloat(0);
        cursor.close();
        return result;
    }

    public int getDaysLeft(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT daysLeft FROM UserBudgetData WHERE user_id = ?", new String[]{String.valueOf(userId)});
        int result = 0;
        if (cursor.moveToFirst()) result = cursor.getInt(0);
        cursor.close();
        return result;
    }

    public String getLastOpenedDate(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT lastOpenedDate FROM UserBudgetData WHERE user_id = ?", new String[]{String.valueOf(userId)});
        String result = "";
        if (cursor.moveToFirst()) result = cursor.getString(0);
        cursor.close();
        return result;
    }

    // Διαγραφή όλων των εξόδων για έναν χρήστη
    public void deleteAllExpensesForUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Transactions", "user_id = ? AND type = 'expense'", new String[]{String.valueOf(userId)});
    }

}

