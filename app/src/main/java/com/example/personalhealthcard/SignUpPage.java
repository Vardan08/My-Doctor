package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SignUpPage extends AppCompatActivity {
    Button signUp;
    Spinner spinner;
    ArrayList spinnerArrList;
    ArrayAdapter spinnerArrAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_sign_up_page);
        TextView alreadyUserTextView = findViewById(R.id.already_user);
        signUp = findViewById(R.id.signUpBtn);
        spinner = findViewById(R.id.spinner);
        spinnerArrList = new ArrayList();
        spinnerArrList.add("Doctor");
        spinnerArrList.add("Patient");
        spinnerArrAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerArrList);
        spinnerArrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(spinnerArrAdapter);

        String textViewText = alreadyUserTextView.getText().toString();

        SpannableString spannableString = new SpannableString(textViewText);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Toast.makeText(SignUpPage.this, "how how how", Toast.LENGTH_SHORT).show();
                openDoctorsLoginPage();
            }
        };

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDoctorsLoginPage();
            }
        });


        int startIndex = textViewText.indexOf("Login here");
        int endIndex = startIndex + "Login here".length();
        spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);

        alreadyUserTextView.setText(spannableString);

        alreadyUserTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
    private void openDoctorsLoginPage() {
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
    }
}