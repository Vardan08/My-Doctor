package com.example.mydoctor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

import com.example.mydoctor.doctors_classes.DoctorsHomePage;
import com.example.mydoctor.forgot_password_classes.ForgotPassword;
import com.example.mydoctor.patients_classes.PatientsHomePage;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginPage extends AppCompatActivity {
    Button login;
    EditText passText;
    TextInputEditText emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();

        TextView createAccountTextView = findViewById(R.id.create_account);
        TextView forgotPasswordTextView = findViewById(R.id.forgot_password);


        setClickableSpan(createAccountTextView, "Create Account", new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(LoginPage.this, "Create Account Clicked", Toast.LENGTH_SHORT).show();
                openSignUpPage();
            }
        });

        setClickableSpan(forgotPasswordTextView, "Forgot Password", new ClickableSpan() {
            @Override
            public void onClick(View widget) {
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
                String email = emailText.getText().toString().trim();
                String password = passText.getText().toString().trim();
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(LoginPage.this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                }else {
                    signIn(email, password);
                }
            }
        });
    }

    private void signIn(String email, String password) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    Log.d("MyTask", "" + task);
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d("myuservalue", "" + user);
                        checkUserRoll(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(LoginPage.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LoginPage.this, "No such user",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginPage.this, "Error checking user roll",
                        Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, SignUpPage.class);
        startActivity(intent);
    }

    private void openDoctorsHomePage() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid()).addSnapshotListener(
                            (snapshot, e) -> {
                                if (e != null) {
                                    Toast.makeText(this, "cant fetch metadata", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                assert snapshot != null:"data snapshot is null when it is not expected";

                                Intent intent = new Intent(LoginPage.this, DoctorsHomePage.class);
                                User userMetadata = snapshot.toObject(User.class);

                                intent.putExtra("USER",userMetadata);

                                startActivity(intent);


                            }
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Error opening LoginPage: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openPatientHomePage() {
        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid()).addSnapshotListener(
                            (snapshot, e) -> {
                                if (e != null) {
                                    Toast.makeText(this, "cant fetch metadata", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                assert snapshot != null:"data snapshot is null when it is not expected";

                                Intent intent = new Intent(LoginPage.this, PatientsHomePage.class);
                                User userMetadata = snapshot.toObject(User.class);

                                intent.putExtra("USER",userMetadata);

                                startActivity(intent);


                            }
                    );
        } catch (Exception e) {
            Toast.makeText(this, "Error opening LoginPage: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    private void openForgotPassword() {
        Intent intent = new Intent(LoginPage.this, ForgotPassword.class);
        startActivity(intent);
    }


}
