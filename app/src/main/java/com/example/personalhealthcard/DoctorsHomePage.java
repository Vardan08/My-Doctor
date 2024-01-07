package com.example.personalhealthcard;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DoctorsHomePage extends AppCompatActivity {
    private FloatingActionButton todayPatientsButton, messagesButton, profileButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctors_home_page);
        todayPatientsButton = findViewById(R.id.todayPatients);
        messagesButton = findViewById(R.id.messages);
        profileButton = findViewById(R.id.profile);
        TodayPatients todayPatients = new TodayPatients();
        setNewFragment(todayPatients);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = new Profile();
                setNewFragment(profile);
            }
        });

        todayPatientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(todayPatients);
            }
        });
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoctorMessages messages = new DoctorMessages();
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