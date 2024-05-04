package com.example.champion_compass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.Arrays;
import java.util.List;

public class sign_in_with_g_activity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        mAuth = FirebaseAuth.getInstance();

        EditText emailEditText = findViewById(R.id.editTextText2);
        EditText passwordEditText = findViewById(R.id.passworEditText);

        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createSignInIntent(); // call method to start the process
            }
        });

        Button button007 = findViewById(R.id.button007);
        button007.setOnClickListener(v -> signInUser(emailEditText.getText().toString(), passwordEditText.getText().toString()));
    }

    public void createSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        // Creating and launching sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Toast.makeText(sign_in_with_g_activity.this, "Successful sign in", Toast.LENGTH_SHORT).show();
            redirectToWelcomeActivity();
        } else {
            // Unsuccessful sign in
            Toast.makeText(sign_in_with_g_activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToWelcomeActivity() {
        Intent intent = new Intent(this, welcome_activity.class);
        startActivity(intent);
        finish();
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();  // Retrieve the userId
                            updateUI(user, userId);  // Pass userId to the updateUI method
                            Toast.makeText(sign_in_with_g_activity.this, "Successful sign in", Toast.LENGTH_SHORT).show();
                        } else {
                            updateUI(null, null);
                        }
                    } else {
                        Toast.makeText(sign_in_with_g_activity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null, null);
                    }
                });
    }

    private void updateUI(FirebaseUser user, String userId) {
        if (user != null) {
            // User is signed in
            Intent intent = new Intent(this, welcome_activity.class);
            intent.putExtra("USER_ID", userId);  // Pass userId to the next activity
            startActivity(intent);
            finish();
        } else {
            // User is not signed in
            Toast.makeText(sign_in_with_g_activity.this, "User not signed in, please Sign in or Sign up", Toast.LENGTH_SHORT).show();
        }
    }


}


