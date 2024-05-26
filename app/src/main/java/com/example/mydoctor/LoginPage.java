package com.example.mydoctor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.mydoctor.data_structures.User;
import com.example.mydoctor.doctors_classes.DoctorsHomePage;
import com.example.mydoctor.forgot_password_classes.ForgotPassword;
import com.example.mydoctor.patients_classes.PatientsHomePage;
import com.example.mydoctor.workers.VaccinationReminderWorker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LoginPage extends AppCompatActivity {
    Button login;
    EditText passText;
    TextInputEditText emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // Check for notification permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            ProgressDialog progressDialog = new ProgressDialog(LoginPage.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(currentUser.getUid());
            docRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("myUserDocument", "" + document);
                        String roll = document.getString("roll"); // Assuming you have a 'roll' field
                        if ("Doctor".equals(roll)) {
                            openDoctorsHomePage();
                            progressDialog.dismiss();
                        } else if ("Patient".equals(roll)) {
                            Data inputData = new Data.Builder()
                                    .putString("userId", currentUser.getUid())
                                    .build();

                            PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(VaccinationReminderWorker.class, 24, TimeUnit.HOURS)
                                    .setInputData(inputData)
                                    .build();

                            WorkManager.getInstance(this).enqueueUniquePeriodicWork("VaccinationReminder", ExistingPeriodicWorkPolicy.REPLACE, periodicWorkRequest);
                            openPatientHomePage();
                            progressDialog.dismiss();
                        }
                    } else {
                        Toast.makeText(LoginPage.this, "No such user", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } else {
                    Toast.makeText(LoginPage.this, "Error checking user roll", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }

        // Initialize UI components
        TextView createAccountTextView = findViewById(R.id.create_account);
        TextView forgotPasswordTextView = findViewById(R.id.forgot_password);

        setClickableSpan(createAccountTextView, "Create Account", new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(LoginPage.this, "Create Account Clicked", Toast.LENGTH_SHORT).show();
                openSignUpPage();
            }
        });

        setClickableSpan(forgotPasswordTextView, "Forgot Password", new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Toast.makeText(LoginPage.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
                openForgotPassword();
            }
        });

        login = findViewById(R.id.loginButton);
        emailText = findViewById(R.id.editTextTextEmailAddress);
        passText = findViewById(R.id.Password);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = Objects.requireNonNull(emailText.getText()).toString().trim();
                String password = passText.getText().toString().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginPage.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                } else {
                    signIn(email, password);
                }
            }
        });
    }

    private void signIn(String email, String password) {
        ProgressDialog progressDialog = new ProgressDialog(LoginPage.this);
        progressDialog.setMessage("Signing in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();

                    Log.d("MyTask", "" + task);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("myuservalue", "" + user);
                        if (user != null && user.isEmailVerified()) {
                            checkUserRoll(user);
                        } else {
                            // Email is not verified
                            Toast.makeText(LoginPage.this, "Email is not verified. Please check your email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRoll(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("myUserDocument", "" + document);
                    String roll = document.getString("roll"); // Assuming you have a 'roll' field
                    if ("Doctor".equals(roll)) {
                        openDoctorsHomePage();
                    } else if ("Patient".equals(roll)) {
                        openPatientHomePage();
                    }
                } else {
                    Toast.makeText(LoginPage.this, "No such user", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginPage.this, "Error checking user roll", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setClickableSpan(TextView textView, String clickableText, ClickableSpan clickableSpan) {
        String textViewText = textView.getText().toString();
        SpannableString spannableString = new SpannableString(textViewText);

        int startIndex = textViewText.toLowerCase().indexOf(clickableText.toLowerCase());
        int endIndex = startIndex + clickableText.length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);

        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void openSignUpPage() {
        emailText.setText("");
        passText.setText("");
        Intent intent = new Intent(this, SignUpPage.class);
        startActivity(intent);
    }

    private void openDoctorsHomePage() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            assert user != null;
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid()).addSnapshotListener(
                            (snapshot, e) -> {
                                if (e != null) {
                                    Toast.makeText(this, "cant fetch metadata", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                assert snapshot != null : "data snapshot is null when it is not expected";

                                Intent intent = new Intent(LoginPage.this, DoctorsHomePage.class);
                                User userMetadata = snapshot.toObject(User.class);

                                intent.putExtra("USER", userMetadata);

                                startActivity(intent);
                                finish();

                            }
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Error opening LoginPage: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPatientHomePage() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            assert user != null;
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid()).addSnapshotListener(
                            (snapshot, e) -> {
                                if (e != null) {
                                    Toast.makeText(this, "cant fetch metadata", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                assert snapshot != null : "data snapshot is null when it is not expected";

                                Intent intent = new Intent(LoginPage.this, PatientsHomePage.class);
                                User userMetadata = snapshot.toObject(User.class);

                                intent.putExtra("USER", userMetadata);

                                startActivity(intent);
                                finish();

                            }
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Error opening LoginPage: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openForgotPassword() {
        emailText.setText("");
        passText.setText("");
        Intent intent = new Intent(LoginPage.this, ForgotPassword.class);
        startActivity(intent);
    }
}
