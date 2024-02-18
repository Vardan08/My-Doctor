package com.example.mydoctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DoctorsHomePage extends AppCompatActivity {
    private FloatingActionButton todayPatientsButton, messagesButton, profileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user = getIntent().getParcelableExtra("USER");



        setContentView(R.layout.activity_doctors_home_page);

        todayPatientsButton = findViewById(R.id.todayPatients);
        messagesButton = findViewById(R.id.messages);
        profileButton = findViewById(R.id.profile);

        TodayPatients todayPatients = new TodayPatients();
        setNewFragment(todayPatients,user);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = new Profile();
                setNewFragment(profile,user);
            }
        });

        todayPatientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(todayPatients,user);
            }
        });

        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoctorMessages messages = new DoctorMessages();
                setNewFragment(messages,user);
            }
        });

        // Add onBackPressedDispatcher callback
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Call the method to handle back press
                exitApplicationIfInTodayPatientsFragment();
            }
        });
    }

    private void setNewFragment(Fragment fragment, User user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER", user);  // Assuming User implements Parcelable
        fragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_of_three_buttons, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }


    private void exitApplicationIfInTodayPatientsFragment() {
        // Check if the current fragment is TodayPatients
        Fragment currentFragment = getCurrentFragment();

        if (currentFragment != null && currentFragment instanceof TodayPatients) {
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
}
