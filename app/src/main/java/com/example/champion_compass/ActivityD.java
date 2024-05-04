package com.example.champion_compass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityD extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.support_lane);

        Button navigateButton = findViewById(R.id.support_lane_button);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start ActivityB
                Intent intent = new Intent(ActivityD.this, welcome_activity.class);
                startActivity(intent);
            }
        });
    }
}
