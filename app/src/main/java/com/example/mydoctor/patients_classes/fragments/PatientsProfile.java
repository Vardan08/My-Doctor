package com.example.mydoctor.patients_classes.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mydoctor.LoginPage;
import com.example.mydoctor.R;
import com.example.mydoctor.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class PatientsProfile extends Fragment {
    private User user;
    TextView fullName,email,phoneNumber,nameBelowPhoto,deleteAccountTextView;
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
        View view = inflater.inflate(R.layout.fragment_patients_profile, container, false);
        fullName = view.findViewById(R.id.textName);
        email = view.findViewById(R.id.textEmail);
        phoneNumber = view.findViewById(R.id.textPhone);
        logOut = view.findViewById(R.id.logOut);
        nameBelowPhoto = view.findViewById(R.id.textView3);
        deleteAccountTextView = view.findViewById(R.id.deleteAccount); // Bind the delete account TextView


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

        deleteAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount(); // Call the delete account method
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        return view;
    }
    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String uid = user.getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "User document deleted successfully.");
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Delete Account", "User account deleted.");
                                FirebaseAuth.getInstance().signOut();
                                openLoginActivity();
                            } else {
                                Log.w("Delete Account", "Failed to delete user account.", task.getException());
                            }
                        });
                    })
                    .addOnFailureListener(e -> Log.w("Firestore", "Error deleting user document", e));
        }else{
            Log.d("Delete Account", "No user to delete.");
        }
    }

    private void openLoginActivity() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getActivity(), LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очистите back stack
        startActivity(intent);
    }
}