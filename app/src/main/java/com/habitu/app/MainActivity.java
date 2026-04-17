package com.habitu.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // Declare variables for all screen elements
    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister;
    FirebaseAuth mAuth; // Firebase login system

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Load the XML screen

        // Connect Firebase
        mAuth = FirebaseAuth.getInstance();

        // Connect Java variables to the XML elements using their IDs
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // If user is already logged in, skip login screen
        if (mAuth.getCurrentUser() != null) {
            goToHome();
            return;
        }

        // What happens when Sign In button is tapped
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Read what the user typed
                String email    = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();


                if (email.isEmpty()) {
                    etEmail.setError("Please enter your email");
                    return;
                }
                if (password.isEmpty()) {
                    etPassword.setError("Please enter your password");
                    return;
                }


                btnLogin.setEnabled(false);
                btnLogin.setText("Signing in...");


                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(result -> {
                            // Success — go to home screen
                            Toast.makeText(MainActivity.this,
                                    "Welcome back!", Toast.LENGTH_SHORT).show();
                            goToHome();
                        })
                        .addOnFailureListener(e -> {
                            // Failed — show error and re-enable button
                            String errorMsg = e.getMessage();
                            if (errorMsg != null && errorMsg.contains("no user record")) {
                                Toast.makeText(MainActivity.this,
                                        "No account found. Please create an account first.",
                                        Toast.LENGTH_LONG).show();
                            } else if (errorMsg != null && errorMsg.contains("password is invalid")) {
                                Toast.makeText(MainActivity.this,
                                        "Incorrect password. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "Login failed. Please check your email and password.",
                                        Toast.LENGTH_LONG).show();
                            }
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Sign In");
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Sign In");
                        });
            }
        });

        // What happens when "Sign up" text is tapped
        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    // Helper method to navigate to home
    private void goToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish(); // Close login screen so back button doesn't return to it
    }
}