package com.example.mydoctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Profile extends Fragment {
    TextView fullName, email, phoneNumber,nameBelowPhoto;
    Button logOutButton;
    private User user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("USER");
            Log.d("Profile",user.getFullName());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_profil, container, false);
        nameBelowPhoto = view.findViewById(R.id.textView3);
        fullName = view.findViewById(R.id.textName);
        email = view.findViewById(R.id.textEmail);
        phoneNumber = view.findViewById(R.id.textPhone);
        logOutButton = view.findViewById(R.id.logOut);

        // Assuming your User class has getFullName(), getEmail(), and getPhoneNumber() methods
        if(user != null) {
            nameBelowPhoto.setText(user.getFullName());
            fullName.setText(user.getFullName()); // Set full name
            email.setText(user.getEmail()); // Set email
            phoneNumber.setText(user.getMobileNumber()); // Set phone number
        } else {
            // Handle the case where user is null if necessary
            Log.d("ProfileFragment", "User data is not available");
        }

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        return view;
    }


    private void openLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginPage.class);
        startActivity(intent);
    }
}
