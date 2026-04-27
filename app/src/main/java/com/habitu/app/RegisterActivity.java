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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Declare all input fields
    EditText etFirstName, etSurname, etPhone, etEmail,
            etUniversity, etPassword, etConfirmPassword;
    Button btnRegister;
    TextView tvLogin;

    // Firebase tools
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Connect Firebase
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        // Connect all fields to their XML IDs
        etFirstName       = findViewById(R.id.etFirstName);
        etSurname         = findViewById(R.id.etSurname);
        etPhone           = findViewById(R.id.etPhone);
        etEmail           = findViewById(R.id.etEmail);
        etUniversity      = findViewById(R.id.etUniversity);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister       = findViewById(R.id.btnRegister);
        tvLogin           = findViewById(R.id.tvLogin);

        // When Create Account button is tapped
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Read all the values the user typed
                String firstName  = etFirstName.getText().toString().trim();
                String surname    = etSurname.getText().toString().trim();
                String phone      = etPhone.getText().toString().trim();
                String email      = etEmail.getText().toString().trim();
                String university = etUniversity.getText().toString().trim();
                String password   = etPassword.getText().toString().trim();
                String confirm    = etConfirmPassword.getText().toString().trim();

                // Check that required fields are not empty
                if (firstName.isEmpty()) {
                    etFirstName.setError("Please enter your first name");
                    return;
                }
                if (surname.isEmpty()) {
                    etSurname.setError("Please enter your surname");
                    return;
                }
                if (phone.isEmpty()) {
                    etPhone.setError("Please enter your phone number");
                    return;
                }
                if (email.isEmpty()) {
                    etEmail.setError("Please enter your email");
                    return;
                }
                if (password.isEmpty()) {
                    etPassword.setError("Please create a password");
                    return;
                }
                if (password.length() < 6) {
                    etPassword.setError("Password must be at least 6 characters");
                    return;
                }
                if (!password.equals(confirm)) {
                    etConfirmPassword.setError("Passwords do not match");
                    return;
                }

                // Disable button while registering
                btnRegister.setEnabled(false);
                btnRegister.setText("Creating account...");

                // Step 1 — Create the user in Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {

                            // Get the unique ID Firebase gave this user
                            String userId = mAuth.getCurrentUser().getUid();

                            // Step 2 — Save extra details to Firestore database
                            Map<String, Object> user = new HashMap<>();
                            user.put("firstName",  firstName);
                            user.put("surname",    surname);
                            user.put("phone",      phone);
                            user.put("email",      email);
                            user.put("university", university);
                            user.put("userId",     userId);

                            // Save to Firestore under "users" collection
                            db.collection("users").document(userId).set(user)
                                    .addOnSuccessListener(unused -> {
                                        // Everything saved — go to home screen
                                        Toast.makeText(RegisterActivity.this,
                                                "Account created! Welcome to HabitU!",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(
                                                RegisterActivity.this, HomeActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Firestore save failed
                                        Toast.makeText(RegisterActivity.this,
                                                "Error saving info: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                        btnRegister.setEnabled(true);
                                        btnRegister.setText("Create Account");
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // Firebase Auth failed
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            btnRegister.setEnabled(true);
                            btnRegister.setText("Create Account");
                        });
            }
        });

        // When "Sign in" is tapped go back to login
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Goes back to login screen
            }
        });
    }
}