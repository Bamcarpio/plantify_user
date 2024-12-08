package com.example.plantify_user;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private MaterialButton materialButton;
    private EditText user_email, user_password;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private TextView RegisterBtnTxt, forgotPasswordTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth and Database instances before anything else
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseApp.initializeApp(this);



        // Check if user is already logged in
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        String savedEmail = sharedPreferences.getString("user_email", "");
        String savedPassword = sharedPreferences.getString("user_password", "");
        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            autoLogin(savedEmail, savedPassword);
            return; // Skip login screen
        }

        // Set up login screen
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        user_email = findViewById(R.id.user_email);
        user_password = findViewById(R.id.user_password);
        materialButton = findViewById(R.id.button_login);
        RegisterBtnTxt = findViewById(R.id.Register);
        forgotPasswordTxt = findViewById(R.id.forgot_password_txt);  // Forgot password text

        // Pre-fill email if saved
        user_email.setText(sharedPreferences.getString("user_email", ""));

        RegisterBtnTxt.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
        });

        materialButton.setOnClickListener(v -> {
            String email = user_email.getText().toString().trim();
            String password = user_password.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            login(email, password);
        });

        forgotPasswordTxt.setOnClickListener(v -> {
            // Open dialog for password reset
            showForgotPasswordDialog();
        });
    }
    private void login(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    saveLoginInfo(email, password);
                    Intent intent = new Intent(MainActivity.this, Home.class);
                    intent.putExtra("userid", user.getUid());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void autoLogin(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser users = firebaseAuth.getCurrentUser();
                Intent intent = new Intent(MainActivity.this, Home.class);
                if (users != null) {
                    intent.putExtra("userid", users.getUid());
                }
                startActivity(intent);
                finish(); // Prevent back navigation to login
            } else {
                Toast.makeText(MainActivity.this, "Failed to auto-login", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLoginInfo(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_email", email);
        editor.putString("user_password", password);
        editor.apply(); // Commit changes
    }

    // Method to show the Forgot Password dialog
    private void showForgotPasswordDialog() {
        final EditText emailInput = new EditText(MainActivity.this);
        emailInput.setHint("Enter your email");

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Forgot Password")
                .setMessage("Enter your email to reset your password.")
                .setView(emailInput)
                .setCancelable(false)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String email = emailInput.getText().toString().trim();
                    if (email.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    } else {
                        sendPasswordResetEmail(email);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    // Method to send password reset email
    private void sendPasswordResetEmail(String email) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                showDialog("Password reset email sent. Please check your inbox.");
            } else {
                showDialog("Failed to send password reset email. Please try again.");
            }
        });
    }

    // Method to show dialog with messages
    private void showDialog(String message) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
}


