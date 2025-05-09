package com.example.brokenomore;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;


import androidx.core.view.WindowInsetsCompat;


public class RegisterActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button registerBtn;
    UserDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        registerBtn = findViewById(R.id.registerBtn);

        dbHelper = new UserDatabaseHelper(this);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Συμπλήρωσε όλα τα πεδία", Toast.LENGTH_SHORT).show();
                } else {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("email", email);
                    values.put("password", password);

                    long result = db.insert("Users", null, values);
                    if (result == -1) {
                        Toast.makeText(RegisterActivity.this, "Ο λογαριασμός υπάρχει ήδη", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Επιτυχής εγγραφή!", Toast.LENGTH_SHORT).show();
                        finish(); // επιστροφή στο login ή αλλού
                    }
                }
            }
        });
    }
}
