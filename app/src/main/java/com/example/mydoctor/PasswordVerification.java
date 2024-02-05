package com.example.mydoctor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordVerification extends AppCompatActivity {
    Button buttonVerifyCode;
    EditText editTextCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_verification);

        buttonVerifyCode = findViewById(R.id.buttonVerifyCode);
        editTextCode = findViewById(R.id.editTextCode);

        buttonVerifyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input from EditText field
                String verificationCode = editTextCode.getText().toString().trim();

                // Check if the entered code is six digits long
                if (verificationCode.length() == 6) {
                    // Proceed with your verification logic
                    // For example, you can compare it with a predefined code
                    if (verificationCode.equals("123456")) {
                        // Code is correct
                        Toast.makeText(PasswordVerification.this, "Verification successful", Toast.LENGTH_SHORT).show();
                        openCreateNewPassWordPage();
                        // Add your further actions here
                    } else {
                        // Code is incorrect
                        Toast.makeText(PasswordVerification.this, "Incorrect verification code", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Show a message if the entered code is not six digits long
                    Toast.makeText(PasswordVerification.this, "Please enter a six-digit code", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void openCreateNewPassWordPage(){
        Intent intent = new Intent(this, CreateNewPassword.class);
        startActivity(intent);
    }
}
