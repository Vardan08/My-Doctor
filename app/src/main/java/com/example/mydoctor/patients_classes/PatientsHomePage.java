package com.example.mydoctor.patients_classes;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

import com.example.mydoctor.R;
import com.example.mydoctor.data_structures.User;
import com.example.mydoctor.patients_classes.fragments.AddVisit;
import com.example.mydoctor.patients_classes.fragments.ChildrenPatientRegistration;
import com.example.mydoctor.patients_classes.fragments.PatientMessages;
import com.example.mydoctor.patients_classes.fragments.PatientsProfile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PatientsHomePage extends AppCompatActivity {
    private FloatingActionButton todayPatientsButton, messagesButton, profileButton, questionnaireButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_home_page);
        User user = getIntent().getParcelableExtra("USER");
        todayPatientsButton = findViewById(R.id.todayPatients);
        messagesButton = findViewById(R.id.messages);
        profileButton = findViewById(R.id.profile);
        questionnaireButton = findViewById(R.id.questionnaireButton);

        AddVisit addVisit = new AddVisit();
        setNewFragment(addVisit,user);


        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatientsProfile profile = new PatientsProfile();
                setNewFragment(profile,user);
            }
        });

        todayPatientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(addVisit,user);
            }
        });
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatientMessages messages = new PatientMessages();
                setNewFragment(messages,user);
            }
        });

        questionnaireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChildrenPatientRegistration childrenPatientRegistration = new ChildrenPatientRegistration();
                setNewFragment(childrenPatientRegistration,user);
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
    private void setNewFragment(Fragment fragment, User user) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("USER", user);  // Assuming User implements Parcelable
        fragment.setArguments(bundle);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_of_three_buttons, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

}