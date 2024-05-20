package com.example.mydoctor.doctors_classes.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;


public class PatientData extends Fragment {

    TextView nameBelowPhoto, fullNameTextView, dobTextView, locationTextView, parrentFullNameTextView, emailTextView, mobileNumberTextView;
    ImageView childImage;
    Map<String,Object> patientChildData;
    Map<String,Object> parentData;
    String childId;
    String parentId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public void setPatientChildData(Map<String,Object> patientChildData,String parentId, String childId) {
        this.patientChildData = patientChildData;
        this.parentId = parentId;
        this.childId = childId;
    }


    public PatientData(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patient_data, container, false);
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading child data...");
        progressDialog.show();
        TextView patientQuestionnaire = view.findViewById(R.id.patientQuestionnaire);
        assert currentUser != null;
        db.collection("requests").whereEqualTo("doctorId",currentUser.getUid()).whereEqualTo("childId",childId).get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document: task.getResult()){
                            String status = document.getString("status");
                            if(status != null){
                                if(status.equals("pending")){
                                    patientQuestionnaire.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });

        patientQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoctorQuestionnaire anotherFragment = new DoctorQuestionnaire();
                anotherFragment.setChildIdAndPatientId(childId,parentId);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
            }
        });
        nameBelowPhoto = view.findViewById(R.id.textView3);
        fullNameTextView = view.findViewById(R.id.textName);
        dobTextView = view.findViewById(R.id.textDob);
        locationTextView = view.findViewById(R.id.location);
        parrentFullNameTextView = view.findViewById(R.id.textParentName);
        emailTextView = view.findViewById(R.id.email);
        mobileNumberTextView = view.findViewById(R.id.mobileNumber);
        String fullName = (String) patientChildData.get("fullName");
        String dob = (String) patientChildData.get("dob");
        String location = (String) patientChildData.get("location");
        childImage = view.findViewById(R.id.profilePhoto);
        if(patientChildData.containsKey("imageUri")){
            String imageUrl = (String) patientChildData.get("imageUri");
            Glide.with(this)
                    .load(imageUrl)
                    .into(childImage);
        }
        nameBelowPhoto.setText(fullName);
        fullNameTextView.setText(fullName);
        dobTextView.setText(dob);
        locationTextView.setText(location);
        db.collection("users").document(parentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if(documentSnapshot.exists()){
                        parentData = documentSnapshot.getData();
                        String parentFullName = (String) parentData.get("fullName");
                        String parentEmail = (String) parentData.get("email");
                        String parentMobileNumber = (String) parentData.get("mobileNumber");
                        parrentFullNameTextView.setText(parentFullName);
                        emailTextView.setText(parentEmail);
                        mobileNumberTextView.setText(parentMobileNumber);
                    }
                    progressDialog.dismiss();
                });
        return view;
    }
}