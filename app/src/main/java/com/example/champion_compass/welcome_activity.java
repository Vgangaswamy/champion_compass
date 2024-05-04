package com.example.champion_compass;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import org.checkerframework.checker.nullness.qual.NonNull;

public class welcome_activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout5);

        Button button5 = findViewById(R.id.button5);
        Button sign_out_button = findViewById(R.id.sign_out_button);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(welcome_activity.this, intro_obj_activity.class);
                startActivity(intent);
            }
        });
        sign_out_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(welcome_activity.this, sign_in_with_g_activity.class);
                signOut();
                startActivity(intent);
            }
        });
    }
    // user sign out
    public void signOut() {
        // [START auth_fui_sign out]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Show a Toast message on successful sign-out
                            Toast.makeText(welcome_activity.this, "Successful sign out", Toast.LENGTH_SHORT).show();

                            // Optionally, you can redirect the user to the sign-in page or elsewhere
                            Intent intent = new Intent(welcome_activity.this, sign_in_with_g_activity.class);
                            startActivity(intent);
                            finish();  // Call finish to remove this activity from the activity stack
                        } else {
                            // If sign-out failed, handle the error
                            // For example, show a different Toast message
                            Toast.makeText(welcome_activity.this, "Sign out failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
