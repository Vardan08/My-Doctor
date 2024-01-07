package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class CreateNewPassword extends AppCompatActivity {
    Button buttonSavePassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);
        buttonSavePassword = findViewById(R.id.buttonSavePassword);
        buttonSavePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginPage();
            }
        });
    }
    private void openLoginPage(){
        Intent intent = new Intent(this,LoginPage.class);
        startActivity(intent);
    }
}