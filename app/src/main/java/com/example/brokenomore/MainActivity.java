package com.example.brokenomore;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.content.Intent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Σύνδεση Buttons
        Button btnAddExpense = findViewById(R.id.btnAddExpense);
        Button btnChallenges = findViewById(R.id.btnChallenges);

        // OnClick για το πρώτο κουμπί
        btnAddExpense.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddExpenseActivity.class);
                startActivity(intent);
            }
        });

        // OnClick για το δεύτερο κουμπί
        btnChallenges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChallengesActivity.class);
                startActivity(intent);
            }
        });
    }


}