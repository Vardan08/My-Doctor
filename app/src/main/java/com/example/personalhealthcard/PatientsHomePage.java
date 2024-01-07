package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PatientsHomePage extends AppCompatActivity {
    private FloatingActionButton todayPatientsButton, messagesButton, profileButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_home_page);
        todayPatientsButton = findViewById(R.id.todayPatients);
        messagesButton = findViewById(R.id.messages);
        profileButton = findViewById(R.id.profile);
        AddVisit addVisit = new AddVisit();
        setNewFragment(addVisit);


        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatientsProfile profile = new PatientsProfile();
                setNewFragment(profile);
            }
        });

        todayPatientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(addVisit);
            }
        });
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatientMessages messages = new PatientMessages();
                setNewFragment(messages);
            }
        });
    }
    private void setNewFragment(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_of_three_buttons, fragment);
        ft.commit();
    }
}