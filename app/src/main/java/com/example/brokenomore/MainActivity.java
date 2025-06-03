package com.example.brokenomore;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    SharedPreferences prefs;
    TextView tvBudget, budgetDisplay;
    EditText budgetInput;
    Button btnSaveBudget, btnChangeBudget;

    TransactionDatabaseHelper transactionDb = new TransactionDatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);

        // Κουμπιά πλοήγησης
        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        Button btnChallenges = findViewById(R.id.btnChallenges);



        btnAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });

        btnChallenges.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChallengesActivity.class);
            startActivity(intent);
        });

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(Calendar.getInstance().getTime());
        transactionDb.deleteOldTransactions();

        // Εμφάνιση υπολοίπου
        tvBudget = findViewById(R.id.tvBudget);
        budgetDisplay = findViewById(R.id.budgetDisplay);
        budgetInput = findViewById(R.id.budgetInput);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnChangeBudget = findViewById(R.id.btnChangeBudget);

        updateBudgetUI();

        btnSaveBudget.setOnClickListener(v -> {
            String input = budgetInput.getText().toString().trim();
            if (!input.isEmpty()) {
                try {
                    float value = Float.parseFloat(input);
                    prefs.edit().putFloat("budget", value).apply();
                    Toast.makeText(MainActivity.this, "Το budget αποθηκεύτηκε!", Toast.LENGTH_SHORT).show();
                    updateBudgetUI();
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Μη έγκυρο ποσό", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Συμπλήρωσε το budget σου!", Toast.LENGTH_SHORT).show();
            }
        });

        btnChangeBudget.setOnClickListener(v -> {
            budgetInput.setVisibility(View.VISIBLE);
            btnSaveBudget.setVisibility(View.VISIBLE);
            btnChangeBudget.setVisibility(View.GONE);
            budgetInput.setText("");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBudgetUI();
    }

    private void updateBudgetUI() {
        float budget = prefs.getFloat("budget", -1f);

        if (budget >= 0) {
            tvBudget.setText("Υπόλοιπο: " + budget + "€");
            budgetDisplay.setText("Τρέχον budget: " + budget + " €");

            budgetInput.setVisibility(View.GONE);
            btnSaveBudget.setVisibility(View.GONE);
            btnChangeBudget.setVisibility(View.VISIBLE);
        } else {
            tvBudget.setText("Δεν έχει οριστεί budget");
            budgetDisplay.setText("Δεν έχεις καταχωρήσει budget ακόμα");

            budgetInput.setVisibility(View.VISIBLE);
            btnSaveBudget.setVisibility(View.VISIBLE);
            btnChangeBudget.setVisibility(View.GONE);
        }
    }
}
