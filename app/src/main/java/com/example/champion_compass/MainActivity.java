package com.example.champion_compass;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the splash screen initially
        setContentView(R.layout.activity_main);

        // Handler to delay the screen transition by 5 seconds (5000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // After the delay, switch to the next layout
                setContentView(R.layout.layout2);
            }
        }, 5000); // milliseconds delay
    }
}
