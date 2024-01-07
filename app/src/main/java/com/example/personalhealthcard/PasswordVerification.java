package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PasswordVerification extends AppCompatActivity {
    Button buttonVerifyCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_verification);
        buttonVerifyCode = findViewById(R.id.buttonVerifyCode);
        buttonVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCreateNewPassWordPage();
            }
        });
    }
    private void openCreateNewPassWordPage(){
        Intent intent = new Intent(this, CreateNewPassword.class);
        startActivity(intent);
    }
}