package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ForgotPassword extends AppCompatActivity {
    Button buttonSendCode;
    EditText editTextPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        buttonSendCode = findViewById(R.id.buttonSendCode);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);

        buttonSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input from EditText field
                String email = editTextPhoneNumber.getText().toString().trim();

                // Validate email format using a regular expression
                if (!isValidEmail(email)) {
                    Toast.makeText(ForgotPassword.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if email is not valid
                }

                openPassVerification();
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void openPassVerification(){
        Intent intent = new Intent(this, PasswordVerification.class);
        startActivity(intent);
    }
}
