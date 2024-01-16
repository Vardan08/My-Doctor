package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginPage extends AppCompatActivity {
    Button doctorsLogin, logInPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();

        TextView createAccountTextView = findViewById(R.id.create_account);
        TextView forgotPasswordTextView = findViewById(R.id.forgot_password);
        logInPatients = findViewById(R.id.logInPatients);

        logInPatients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPatientHomePage();
            }
        });

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

        doctorsLogin = findViewById(R.id.loginButton);

        doctorsLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDoctorsHomePage();
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

    private void openDoctorsHomePage(){
        try {
            Intent intent = new Intent(LoginPage.this, DoctorsHomePage.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error opening LoginPage: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openForgotPassword(){
        Intent intent = new Intent(LoginPage.this, ForgotPassword.class);
        startActivity(intent);
    }


    private void openPatientHomePage(){
        Intent intent = new Intent(this, PatientsHomePage.class);
        startActivity(intent);
    }
}
