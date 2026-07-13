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
import com.example.mydoctor.patients_classes.fragments.PatientsProfile;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PatientsHomePage extends AppCompatActivity {
    private FloatingActionButton todayPatientsButton, profileButton, questionnaireButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patients_home_page);
        User user = getIntent().getParcelableExtra("USER");
        todayPatientsButton = findViewById(R.id.todayPatients);
        profileButton = findViewById(R.id.profile);
        questionnaireButton = findViewById(R.id.questionnaireButton);

        // Only add the starting fragment on a fresh launch; on rotation the FragmentManager
        // restores it, and adding again would stack a duplicate on top.
        if (savedInstanceState == null) {
            setNewFragment(new AddVisit(), user);
        }

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(new PatientsProfile(), user);
            }
        });

        todayPatientsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // A fresh instance each time: setArguments() throws IllegalStateException
                // if the fragment is already added and its state has been saved.
                setNewFragment(new AddVisit(), user);
            }
        });
        questionnaireButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewFragment(new ChildrenPatientRegistration(), user);
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