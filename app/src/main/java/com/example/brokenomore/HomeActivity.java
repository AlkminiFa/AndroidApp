package com.example.brokenomore;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView budgetAmount;
    private TextView daysInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Συνδέσεις UI
        budgetAmount = findViewById(R.id.budgetAmount);
        daysInfoText = findViewById(R.id.daysInfoText);
        Button changeBudgetBtn = findViewById(R.id.changeBudgetBtn);
        Button addExpenseBtn = findViewById(R.id.addExpenseBtn);
        Button nextDayBtn = findViewById(R.id.nextDayBtn);

        // Κουμπί αλλαγής budget & ημερών
        changeBudgetBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Καταχώρηση Budget και Ημερών");

            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(50, 20, 50, 20);

            final EditText inputBudget = new EditText(this);
            inputBudget.setHint("Ποσό σε €");
            inputBudget.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            layout.addView(inputBudget);

            final EditText inputDays = new EditText(this);
            inputDays.setHint("Πλήθος ημερών");
            inputDays.setInputType(InputType.TYPE_CLASS_NUMBER);
            layout.addView(inputDays);

            builder.setView(layout);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String budgetText = inputBudget.getText().toString();
                String daysText = inputDays.getText().toString();

                if (!budgetText.isEmpty() && !daysText.isEmpty()) {
                    float newBudget = Float.parseFloat(budgetText);
                    int newDays = Integer.parseInt(daysText);

                    SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putFloat("budget", newBudget)
                            .putInt("daysLeft", newDays)
                            .apply();

                    budgetAmount.setText(String.format(Locale.getDefault(), "%.2f €", newBudget));
                    updateDaysText(newDays);
                }
            });

            builder.setNegativeButton("Άκυρο", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        // Κουμπί καταχώρησης εξόδου
        addExpenseBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });

        // Κουμπί προώθησης ημέρας
        nextDayBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Ημέρα προωθήθηκε ✅", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBudget();

        int days = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE)
                .getInt("daysLeft", 30); // προεπιλογή 30
        updateDaysText(days);
    }

    private void refreshBudget() {
        SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);
        float currentBudget = prefs.getFloat("budget", 0.0f);
        budgetAmount.setText(String.format(Locale.getDefault(), "%.2f €", currentBudget));
    }

    private void updateDaysText(int days) {
        String fullText = "Απομένουν " + days + " ημέρες";
        int start = fullText.indexOf(String.valueOf(days));
        int end = start + String.valueOf(days).length();

        SpannableString spannable = new SpannableString(fullText);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
        spannable.setSpan(new RelativeSizeSpan(1.6f), start, end, 0);
        daysInfoText.setText(spannable);
    }
}
