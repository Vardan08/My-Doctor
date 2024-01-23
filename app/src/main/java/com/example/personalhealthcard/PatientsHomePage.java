package com.example.personalhealthcard;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Call the method to handle back press
                exitApplicationIfInTodayPatientsFragment();
            }
        });
    }

    private void exitApplicationIfInTodayPatientsFragment() {
        // Check if the current fragment is TodayPatients
        Fragment currentFragment = getCurrentFragment();

        if (currentFragment != null && currentFragment instanceof AddVisit) {
            // If yes, finish the activity and exit the application
            finishAffinity();
        } else {
            // If not, pop the fragment from the back stack or finish the activity
            FragmentManager fragmentManager = getSupportFragmentManager();
            int backStackEntryCount = fragmentManager.getBackStackEntryCount();

            if (backStackEntryCount > 0) {
                // If there are fragments in the back stack, pop the stack
                fragmentManager.popBackStack();
            } else {
                // If the back stack is empty, finish the activity
                finish();
            }
        }
    }

    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.content_of_three_buttons);
    }
    private void setNewFragment(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_of_three_buttons, fragment);
        ft.commit();
    }
}