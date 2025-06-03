
package com.example.brokenomore;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.*;

public class HomeActivity extends AppCompatActivity {

    private TextView budgetAmount;
    private TextView daysInfoText;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        budgetAmount = findViewById(R.id.budgetAmount);
        daysInfoText = findViewById(R.id.daysInfoText);
        Button changeBudgetBtn = findViewById(R.id.changeBudgetBtn);
        Button addExpenseBtn = findViewById(R.id.addExpenseBtn);
        Button nextDayBtn = findViewById(R.id.nextDayBtn);
        nextDayBtn.setText("📅 Πρόκληση Ημέρας");

        SharedPreferences loginPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = loginPrefs.getInt("userId", -1);

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

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();

                    editor.putFloat("budget_user_" + userId, newBudget);
                    editor.putFloat("initialBudget_user_" + userId, newBudget);
                    editor.putInt("daysLeft_user_" + userId, newDays);
                    editor.putString("lastOpenedDate_user_" + userId, today);

                    String[] categories = {"Καφές", "Φαγητό", "Μετακίνηση", "Διασκέδαση", "Άλλο"};
                    for (String category : categories) {
                        editor.putFloat("spent_" + category + "_user_" + userId, 0f);
                    }

                    editor.apply();
                    budgetAmount.setText(String.format(Locale.getDefault(), "%.2f €", newBudget));
                    showCategoryProgress(newBudget);
                    updateDaysText(newDays);
                }
            });

            builder.setNegativeButton("Άκυρο", (dialog, which) -> dialog.cancel());
            builder.show();
        });

        addExpenseBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddExpenseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);

        int daysLeft = prefs.getInt("daysLeft_user_" + userId, 0);
        String lastOpened = prefs.getString("lastOpenedDate_user_" + userId, "");
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!lastOpened.equals(today)) {
            try {
                Date lastDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastOpened);
                Date currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today);
                long diff = currentDate.getTime() - lastDate.getTime();
                int daysPassed = (int) (diff / (1000 * 60 * 60 * 24));

                if (daysPassed > 0 && daysLeft > 0) {
                    daysLeft = Math.max(0, daysLeft - daysPassed);
                    prefs.edit().putInt("daysLeft_user_" + userId, daysLeft).apply();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            prefs.edit().putString("lastOpenedDate_user_" + userId, today).apply();
        }

        updateDaysText(daysLeft);
        float totalBudget = prefs.getFloat("initialBudget_user_" + userId, 0.0f);
        showCategoryProgress(totalBudget);
        refreshBudget();
    }

    private void refreshBudget() {
        SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);
        float currentBudget = prefs.getFloat("budget_user_" + userId, 0.0f);
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

    private void showCategoryProgress(double totalBudget) {
        LinearLayout container = findViewById(R.id.categoryProgressContainer);
        container.removeAllViews();

        Map<String, Double> expenses = getExpensesGroupedByCategory();

        String[][] categories = {
                {"Καφές", "☕", "#6D4C41"},
                {"Φαγητό", "🍕", "#EF6C00"},
                {"Μετακίνηση", "🚗", "#039BE5"},
                {"Διασκέδαση", "🎉", "#8E24AA"},
                {"Άλλο", "📦", "#607D8B"}
        };

        for (String[] cat : categories) {
            String category = cat[0];
            String emoji = cat[1];
            String colorHex = cat[2];

            double amount = expenses.containsKey(category) ? expenses.get(category) : 0.0;
            double percent = (totalBudget == 0.0) ? 0.0 : (amount / totalBudget) * 100.0;

            TextView label = new TextView(this);
            label.setText(emoji + " " + category + ": " + String.format(Locale.getDefault(), "%.1f", percent) + "%");
            label.setTextSize(16);
            label.setPadding(0, 12, 0, 0);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            bar.setMax(100);
            bar.setProgress((int) percent);
            bar.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            bar.getProgressDrawable().setColorFilter(android.graphics.Color.parseColor(colorHex),
                    android.graphics.PorterDuff.Mode.SRC_IN);

            container.addView(label);
            container.addView(bar);
        }
    }

    private Map<String, Double> getExpensesGroupedByCategory() {
        SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);
        Map<String, Double> map = new HashMap<>();

        String[] categories = {"Καφές", "Φαγητό", "Μετακίνηση", "Διασκέδαση", "Άλλο"};
        for (String category : categories) {
            float amount = prefs.getFloat("spent_" + category + "_user_" + userId, 0f);
            map.put(category, (double) amount);
        }

        return map;
    }
}
