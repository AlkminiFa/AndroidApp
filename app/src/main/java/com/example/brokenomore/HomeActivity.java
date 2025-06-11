
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.widget.Toolbar;


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

        //μενού
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

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
        nextDayBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ChallengesActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });





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

                    TransactionDatabaseHelper dbHelper = new TransactionDatabaseHelper(HomeActivity.this);
                    today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    dbHelper.saveOrUpdateUserBudget(userId, newBudget, newBudget, newDays, today);
                    dbHelper.deleteAllExpensesForUser(userId);//μηδενίζονται οι μπάρες


                    budgetAmount.setText(String.format(Locale.getDefault(), "%.2f €", newBudget));
                    showCategoryProgress(newBudget);
                    updateDaysText(newDays);
                    updateAvatar();//avatar

                }
            });

            builder.setNegativeButton("Άκυρο", (dialog, which) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                // Αντικατέστησε τα παρακάτω με όποια χρώματα θες
                positiveButton.setTextColor(android.graphics.Color.parseColor("#004225"));
                negativeButton.setTextColor(android.graphics.Color.parseColor("#004225"));
            });
            dialog.show();



        });


        addExpenseBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, AddExpenseActivity.class);
            startActivity(intent);
            updateAvatar();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TransactionDatabaseHelper dbHelper = new TransactionDatabaseHelper(HomeActivity.this);
        int daysLeft = dbHelper.getDaysLeft(userId);


        String lastOpened = dbHelper.getLastOpenedDate(userId);
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!lastOpened.equals(today)) {
            try {
                Date lastDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(lastOpened);
                Date currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(today);
                long diff = currentDate.getTime() - lastDate.getTime();
                int daysPassed = (int) (diff / (1000 * 60 * 60 * 24));

                if (daysPassed > 0 && daysLeft > 0) {
                    daysLeft = Math.max(0, daysLeft - daysPassed);
                    lastOpened = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    float budget = dbHelper.getBudget(userId);
                    float initialBudget = dbHelper.getInitialBudget(userId);

// Ενημερώνουμε την εγγραφή με νέο daysLeft και lastOpenedDate
                    dbHelper.saveOrUpdateUserBudget(userId, budget, initialBudget, daysLeft, lastOpened);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            float budget = dbHelper.getBudget(userId);
            float initialBudget = dbHelper.getInitialBudget(userId);
            dbHelper.saveOrUpdateUserBudget(userId, budget, initialBudget, daysLeft, today);

        }


        updateDaysText(daysLeft);
        updateAvatar(); //avatar
        float totalBudget = dbHelper.getInitialBudget(userId);
        showCategoryProgress(totalBudget);
        refreshBudget();


    }

    private void refreshBudget() {
        TransactionDatabaseHelper dbHelper = new TransactionDatabaseHelper(HomeActivity.this);
        float currentBudget = dbHelper.getBudget(userId);
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
                {"Φαγητό", "\uD83C\uDF54", "#EF6C00"},
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
        Map<String, Double> map = new HashMap<>();
        TransactionDatabaseHelper dbHelper = new TransactionDatabaseHelper(this);

        String[] categories = {"Καφές", "Φαγητό", "Μετακίνηση", "Διασκέδαση", "Άλλο"};
        for (String category : categories) {
            double total = dbHelper.getTotalSpentByCategory(userId, category);
            map.put(category, total);
        }

        return map;
    }

    //avatar
    private void updateAvatar() {
        ImageView avatar = findViewById(R.id.avatarImage);

        TransactionDatabaseHelper dbHelper = new TransactionDatabaseHelper(this);
        float budget = dbHelper.getBudget(userId);
        int daysLeft = dbHelper.getDaysLeft(userId);

        // αποφυγή διαιρέσεων με 0
        if (daysLeft == 0 && budget==0) {
            avatar.setImageResource(R.drawable.dead);
            return;
        }

        if (daysLeft <= 0 && budget>0) {
            avatar.setImageResource(R.drawable.happy);
            return;

        }

        double moneyPerDay = budget / daysLeft;

        if (moneyPerDay >= 15) {
            avatar.setImageResource(R.drawable.happy);
        } else if (moneyPerDay >= 10) {
            avatar.setImageResource(R.drawable.normal);
        } else if (moneyPerDay >= 5) {
            avatar.setImageResource(R.drawable.sceptic);
        } else if (moneyPerDay >= 3) {
            avatar.setImageResource(R.drawable.angry);
        } else if (moneyPerDay > 0){
            avatar.setImageResource(R.drawable.sad);
        }
        else {
            avatar.setImageResource(R.drawable.dead);

        }
    }

    // Μενού 3 τελίτσες
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.menu_logout) {
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
