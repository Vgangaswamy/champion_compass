package com.example.champion_compass;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_activity);

        // Handler to delay the screen transition by 3 seconds (3000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // After the delay, switch to the SecondActivity
                Intent intent = new Intent(SplashActivity.this, firebaseActivity.class);
                startActivity(intent);
                // Finish the current activity
                finish();
            }
        }, 3000); // milliseconds delay
    }
}
