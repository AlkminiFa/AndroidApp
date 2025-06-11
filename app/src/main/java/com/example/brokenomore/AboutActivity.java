package com.example.brokenomore;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Εμφάνιση βελάκι πίσω
            getSupportActionBar().setTitle("Σχετικά με την εφαρμογή");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Πίσω στο προηγούμενο Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
