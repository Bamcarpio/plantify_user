package com.example.plantify_user;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Register extends AppCompatActivity {

    private MaterialButton materialButton;
    private EditText user_email, user_password, user_name, user_address, user_contact;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private TextView loginhere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        user_email = findViewById(R.id.user_email);
        user_password = findViewById(R.id.user_password);
        user_name = findViewById(R.id.user_name);
        user_address = findViewById(R.id.user_address);
        user_contact = findViewById(R.id.user_contact);
        loginhere = findViewById(R.id.LoginHere);

        materialButton = findViewById(R.id.Register_btn);

        loginhere.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
        });

        materialButton.setOnClickListener(v -> {
            String email = user_email.getText().toString();
            String pass = user_password.getText().toString();
            String name = user_name.getText().toString();
            String address = user_address.getText().toString();
            String contact = user_contact.getText().toString();

            if (email.isEmpty() || pass.isEmpty() || name.isEmpty() || address.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "Fill in all the fields", Toast.LENGTH_SHORT).show();
            } else {
                register(email, pass, name, address, contact);
            }
        });
    }

    private void register(String email, String password, String username, String address, String contact) {
        // Sign out any current session first
        firebaseAuth.signOut();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification().addOnCompleteListener(emailTask -> {
                                if (emailTask.isSuccessful()) {
                                    Map<String, String> RegisterUser = new HashMap<>();
                                    RegisterUser.put("username", username);
                                    RegisterUser.put("address", address);
                                    RegisterUser.put("Contact", contact);
                                    RegisterUser.put("Profile", "");

                                    firebaseDatabase.getReference().child("Users").child(user.getUid()).setValue(RegisterUser)
                                            .addOnCompleteListener(dbTask -> {
                                                showDialog("Verification email sent. Please verify your email to complete registration.");
                                                firebaseAuth.signOut();

                                                new Handler().postDelayed(() -> {
                                                    Intent intent = new Intent(Register.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }, 2000);
                                            });
                                } else {
                                    showDialog("Failed to send verification email.");
                                }
                            });
                        }
                    } else {
                        showDialog("Account already exists or invalid email.");
                    }
                });
    }

    private void showDialog(String message) {
        new AlertDialog.Builder(Register.this)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, id) -> dialog.dismiss())
                .create()
                .show();
    }
}
