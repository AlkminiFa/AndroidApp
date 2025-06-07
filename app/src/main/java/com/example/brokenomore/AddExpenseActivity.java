
package com.example.brokenomore;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        SharedPreferences loginPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        userId = loginPrefs.getInt("userId", -1);
        updateAvatar();
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        EditText etAmount = findViewById(R.id.etAmount);
        Button btnSave = findViewById(R.id.btnSaveExpense);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountText = etAmount.getText().toString().trim();
                String selectedCategory = spinnerCategory.getSelectedItem().toString();

                if (selectedCategory.equals("Επέλεξε κατηγορία")) {
                    Toast.makeText(AddExpenseActivity.this, "Παρακαλώ επίλεξε κατηγορία", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (amountText.isEmpty()) {
                    Toast.makeText(AddExpenseActivity.this, "Συμπλήρωσε ποσό", Toast.LENGTH_SHORT).show();
                    return;
                }

                float amount;
                try {
                    amount = Float.parseFloat(amountText);
                } catch (NumberFormatException e) {
                    Toast.makeText(AddExpenseActivity.this, "Μη έγκυρο ποσό", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences loginPrefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                int userId = loginPrefs.getInt("userId", -1);

                if (userId == -1) {
                    Toast.makeText(AddExpenseActivity.this, "Πρόβλημα με την ταυτοποίηση χρήστη (userId = -1)", Toast.LENGTH_LONG).show();
                    return;
                }

                SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);
                TransactionDatabaseHelper dbHelper = new TransactionDatabaseHelper(AddExpenseActivity.this);
                float currentBudget = dbHelper.getBudget(userId);
                if (amount > currentBudget) {
                    Toast.makeText(AddExpenseActivity.this, "Το ποσό ξεπερνά το διαθέσιμο budget!", Toast.LENGTH_SHORT).show();
                    return;
                }

                float updatedBudget = currentBudget - amount;
                float initialBudget = dbHelper.getInitialBudget(userId);
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                dbHelper.saveOrUpdateUserBudget(userId, updatedBudget, initialBudget, dbHelper.getDaysLeft(userId), today);

                String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                boolean success = dbHelper.insertTransaction(
                        userId,
                        amount,
                        "expense",
                        selectedCategory,
                        "",
                        currentDate
                );

                if (success) {
                    Toast.makeText(AddExpenseActivity.this,
                            "✅ Καταχωρήθηκε στη ΒΑΣΗ για userId=" + userId + ": " + amount + "€ για " + selectedCategory,
                            Toast.LENGTH_LONG).show();

                    // 🎯 Ενημέρωσε το avatar μετά την επιτυχή καταχώρηση
                    updateAvatar();
                } else {
                    Toast.makeText(AddExpenseActivity.this,
                            "❌ Αποτυχία καταχώρησης στη ΒΑΣΗ για userId=" + userId,
                            Toast.LENGTH_LONG).show();
                }

                String categoryKey = "spent_" + selectedCategory + "_user_" + userId;
                float previous = prefs.getFloat(categoryKey, 0f);
                prefs.edit().putFloat(categoryKey, previous + amount).apply();

                finish();
            }
        });



        updateAvatar();

    }

    //avatar
    private void updateAvatar() {
        ImageView avatar = findViewById(R.id.avatarImage);
        TextView comment = findViewById(R.id.avatarComment);  // 👈 νέο TextView

        TransactionDatabaseHelper dbHelper = new TransactionDatabaseHelper(this);
        float budget = dbHelper.getBudget(userId);
        int daysLeft = dbHelper.getDaysLeft(userId);

        if (daysLeft <= 0) {
            avatar.setImageResource(R.drawable.angry);
            comment.setText("Μηδέν ημέρες; Πεινάμε! 🐷");
            return;
        }

        double moneyPerDay = budget / daysLeft;

        if (moneyPerDay >= 15) {
            avatar.setImageResource(R.drawable.happy);
            comment.setText("-Το πορτοφόλι σου σε φωνάζει βασιλιά!");
        } else if (moneyPerDay >= 10) {
            avatar.setImageResource(R.drawable.normal);
            comment.setText("-Όλα under control!");
        } else if (moneyPerDay >= 5) {
            avatar.setImageResource(R.drawable.sceptic);
            comment.setText("-Τα έξοδα σου φωνάζουν 'σκέψου καλύτερα'!");
        } else if (moneyPerDay >= 3) {
            avatar.setImageResource(R.drawable.angry);
            comment.setText("-Ποιος άνοιξε πάλι το πορτοφόλι σου;!");
        } else {
            avatar.setImageResource(R.drawable.sad);
            comment.setText("-Ώρα να πουλήσεις βιβλία (ή νεφρό).");
        }
    }

}
