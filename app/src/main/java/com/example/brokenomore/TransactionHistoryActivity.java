package com.example.brokenomore;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private TransactionDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private com.example.brokenomore.TransactionAdapter adapter;
    private List<com.example.brokenomore.TransactionItem> transactionList;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);




        recyclerView = findViewById(R.id.recyclerViewTransactions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new TransactionDatabaseHelper(this);

        transactionList = new ArrayList<>();

        loadTransactions();

        adapter = new com.example.brokenomore.TransactionAdapter(transactionList);
        recyclerView.setAdapter(adapter);
    }

    private void loadTransactions() {
        // Παίρνουμε το userId από SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId == -1) {
            // Δεν υπάρχει έγκυρο userId, δεν φορτώνουμε τίποτα
            return;
        }

        Cursor cursor = null;

        try {
            cursor = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT category, date, amount FROM Transactions WHERE user_id = ? ORDER BY date DESC",
                    new String[]{String.valueOf(userId)}
            );

            if (cursor != null && cursor.moveToFirst()) {
                int categoryIndex = cursor.getColumnIndex("category");
                int dateIndex = cursor.getColumnIndex("date");
                int amountIndex = cursor.getColumnIndex("amount");

                do {
                    String category = cursor.getString(categoryIndex);
                    String date = cursor.getString(dateIndex);
                    double amount = cursor.getDouble(amountIndex);

                    transactionList.add(new TransactionItem(category, date, amount));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
