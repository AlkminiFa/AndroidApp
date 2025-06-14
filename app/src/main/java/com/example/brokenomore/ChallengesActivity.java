package com.example.brokenomore;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ChallengesActivity extends AppCompatActivity {

    ImageView wheelImage;
    Button spinButton;
    TextView challengeText;

    String[] challenges = {
            "No spend day...Το έχεις!",
            "No delivery day",
            "Μία μέρα χώρις καφέ απ'έξω!",
            "Μην πάρεις τίποτα με πάνω από 2€ σήμερα.",
            "Απόψε γίνε ο φίλος που λέει <<μήπως να μην βγούμε>>...",
            "Σήμερα κάνε λίστα πριν πας στο σούπερ μάρκετ.",
            "Σήμερα ας φάμε στην λέσχη...",
            "Η φτώχεια θέλει καλοπέραση, για αυτό βγες μία βόλτα χώρις πορτοφόλι!"
    };

    Random random;
    int degree = 0;
    boolean alreadySpunToday = false;
    String todayDate;

    TransactionDatabaseHelper dbHelper;
    int userId; // Διάβασε από SharedPreferences όπως ήδη κάνεις

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);
        Toolbar toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);     // βελάκι πίσω
            getSupportActionBar().setDisplayShowTitleEnabled(false);   // όχι default τίτλος
        }


        wheelImage = findViewById(R.id.wheelImage);
        spinButton = findViewById(R.id.spinButton);
        challengeText = findViewById(R.id.challengeText);

        random = new Random();
        dbHelper = new TransactionDatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String savedChallenge = dbHelper.getChallengeForDate(userId, todayDate);
        if (savedChallenge != null) {
            alreadySpunToday = true;
            challengeText.setText(savedChallenge);
            spinButton.setEnabled(false);
            spinButton.setText("Ήδη επιλέχθηκε");
        }

        spinButton.setOnClickListener(v -> {
            if (!alreadySpunToday) {
                spinWheel();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void spinWheel() {
        int newDegree = random.nextInt(3600) + 360;
        RotateAnimation rotate = new RotateAnimation(degree, newDegree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(3000);
        rotate.setFillAfter(true);
        degree = newDegree;

        rotate.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }

            @Override
            public void onAnimationEnd(Animation animation) {
                int normalizedDegree = degree % 360;
                int sectorSize = 360 / challenges.length;
                int index = normalizedDegree / sectorSize;
                String challenge = challenges[index];

                challengeText.setText(challenge);
                dbHelper.saveChallengeForDate(userId, todayDate, challenge);
                spinButton.setEnabled(false);
                spinButton.setText("Ήδη επιλέχθηκε");
            }


            @Override public void onAnimationRepeat(Animation animation) { }
        });

        wheelImage.startAnimation(rotate);
    }

    private int getUserIdFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("userId", -1);
    }
}
