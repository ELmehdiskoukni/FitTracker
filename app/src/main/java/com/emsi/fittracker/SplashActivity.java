package com.emsi.fittracker;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.emsi.fittracker.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;
    private Handler splashHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize handler
        splashHandler = new Handler(Looper.getMainLooper());

        // Start splash timer
        startSplashTimer();
    }

    private void startSplashTimer() {
        splashHandler.postDelayed(this::checkUserAuthentication, SPLASH_DELAY);
    }

    private void checkUserAuthentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Intent intent;
        if (currentUser != null && currentUser.isEmailVerified()) {
            // User is signed in and email is verified, go to MainActivity
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // No user or email not verified, go to LoginActivity
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
    }
}