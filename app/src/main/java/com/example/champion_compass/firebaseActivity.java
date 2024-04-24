package com.example.champion_compass;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class firebaseActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);  // Your custom sign-up layout

        mAuth = FirebaseAuth.getInstance();

        EditText emailEditText = findViewById(R.id.editTextEmail);
        EditText passwordEditText = findViewById(R.id.editTextPassword);
        Button signUpButton = findViewById(R.id.buttonSignUp);
        Button signInButton = findViewById(R.id.buttonSignIn);  // Assuming you have this in your layout

        signUpButton.setOnClickListener(v -> registerUser(emailEditText.getText().toString(), passwordEditText.getText().toString()));
        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, sign_in_with_g_activity.class);
            startActivity(intent);
        });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Toast.makeText(firebaseActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // User is signed in
            Intent intent = new Intent(this, welcome_activity.class);  // Navigate to another activity
            startActivity(intent);
            finish();
        } else {
            // User is not signed in
            // Optionally reset text fields or update the UI
        }
    }
}
