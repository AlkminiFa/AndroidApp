package com.example.brokenomore;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddExpenseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        EditText etAmount = findViewById(R.id.etAmount);
        Button btnSave = findViewById(R.id.btnSaveExpense);

        // Γέμισμα Spinner με τις κατηγορίες
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.expense_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // Πάτημα στο "Καταχώρηση"
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

                // Χρήση ίδιου SharedPreferences ονόματος με HomeActivity
                SharedPreferences prefs = getSharedPreferences("BrokeNoMorePrefs", MODE_PRIVATE);
                float currentBudget = prefs.getFloat("budget", 0f);

                if (amount > currentBudget) {
                    Toast.makeText(AddExpenseActivity.this, "Το ποσό ξεπερνά το διαθέσιμο budget!", Toast.LENGTH_SHORT).show();
                    return;
                }

                float updatedBudget = currentBudget - amount;
                prefs.edit().putFloat("budget", updatedBudget).apply();

                Toast.makeText(AddExpenseActivity.this,
                        "Καταχωρήθηκε: " + amount + "€ για " + selectedCategory + "\nΥπόλοιπο: " + updatedBudget + "€",
                        Toast.LENGTH_LONG).show();

                // Αποθήκευση ποσού στην αντίστοιχη κατηγορία (προσωρινό, μέχρι να γίνει βάση SQL)
                String key = "spent_" + selectedCategory;
                float previous = prefs.getFloat(key, 0f);
                prefs.edit().putFloat(key, previous + amount).apply();


                finish(); // Επιστροφή στην HomeActivity
            }
        });
    }
}
