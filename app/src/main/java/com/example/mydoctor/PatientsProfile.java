package com.example.mydoctor;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
public class PatientsProfile extends Fragment {
    private User user;
    TextView fullName,email,phoneNumber,nameBelowPhoto;
    Button logOut;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("USER");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_profil, container, false);
        fullName = view.findViewById(R.id.textName);
        email = view.findViewById(R.id.textEmail);
        phoneNumber = view.findViewById(R.id.textPhone);
        logOut = view.findViewById(R.id.logOut);
        nameBelowPhoto = view.findViewById(R.id.textView3);

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

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        return view;
    }

    private void openLoginActivity() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getActivity(), LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очистите back stack
        startActivity(intent);
    }
}