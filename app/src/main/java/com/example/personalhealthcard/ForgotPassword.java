package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ForgotPassword extends AppCompatActivity {
    Button buttonSendCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        buttonSendCode = findViewById(R.id.buttonSendCode);
        buttonSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPassVerification();
            }
        });
    }
    private void openPassVerification(){
        Intent intent = new Intent(this, PasswordVerification.class);
        startActivity(intent);
    }
}