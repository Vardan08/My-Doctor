package com.example.mydoctor.doctors_classes.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;


public class PatientData extends Fragment {

    private static final String ARG_PARENT_ID = "parentId";
    private static final String ARG_CHILD_ID = "childId";

    TextView nameBelowPhoto, fullNameTextView, dobTextView, locationTextView, parrentFullNameTextView, emailTextView, mobileNumberTextView;
    ImageView childImage;
    String childId;
    String parentId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    public PatientData(){}

    /**
     * The child is re-read from Firestore rather than handed over in memory, so the fragment can
     * rebuild itself after a rotation or process death from its arguments alone.
     */
    public static PatientData newInstance(String parentId, String childId) {
        PatientData fragment = new PatientData();
        Bundle args = new Bundle();
        args.putString(ARG_PARENT_ID, parentId);
        args.putString(ARG_CHILD_ID, childId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            parentId = getArguments().getString(ARG_PARENT_ID);
            childId = getArguments().getString(ARG_CHILD_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_patient_data, container, false);

        nameBelowPhoto = view.findViewById(R.id.textView3);
        fullNameTextView = view.findViewById(R.id.textName);
        dobTextView = view.findViewById(R.id.textDob);
        locationTextView = view.findViewById(R.id.location);
        parrentFullNameTextView = view.findViewById(R.id.textParentName);
        emailTextView = view.findViewById(R.id.email);
        mobileNumberTextView = view.findViewById(R.id.mobileNumber);
        childImage = view.findViewById(R.id.profilePhoto);
        TextView patientQuestionnaire = view.findViewById(R.id.patientQuestionnaire);

        if (currentUser == null || parentId == null || childId == null) {
            Toast.makeText(getContext(), "Patient data is unavailable", Toast.LENGTH_SHORT).show();
            return view;
        }

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading child data...");
        progressDialog.show();

        patientQuestionnaire.setOnClickListener(v -> {
            DoctorQuestionnaire anotherFragment = DoctorQuestionnaire.newInstance(childId, parentId);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        db.collection("requests")
                .whereEqualTo("doctorId", currentUser.getUid())
                .whereEqualTo("childId", childId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!isAdded()) return;
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if ("pending".equals(document.getString("status"))) {
                                patientQuestionnaire.setVisibility(View.GONE);
                            }
                        }
                    }
                });

        db.collection("users").document(parentId)
                .collection("children").document(childId)
                .get()
                .addOnSuccessListener(childSnapshot -> {
                    if (!isAdded()) return;
                    progressDialog.dismiss();

                    Map<String, Object> childData = childSnapshot.getData();
                    if (childData == null) {
                        Toast.makeText(getContext(), "Child not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    bindChild(childData);
                    loadParent();
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Failed to load child data", Toast.LENGTH_SHORT).show();
                });

        return view;
    }

    private void bindChild(Map<String, Object> childData) {
        String fullName = (String) childData.get("fullName");
        String dob = (String) childData.get("dob");
        String location = (String) childData.get("location");

        nameBelowPhoto.setText(fullName);
        fullNameTextView.setText(fullName);
        dobTextView.setText(dob);
        locationTextView.setText(location);

        if (childData.containsKey("imageUri")) {
            Glide.with(this)
                    .load((String) childData.get("imageUri"))
                    .into(childImage);
        }
    }

    private void loadParent() {
        db.collection("users").document(parentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!isAdded() || !documentSnapshot.exists()) return;

                    parrentFullNameTextView.setText(documentSnapshot.getString("fullName"));
                    emailTextView.setText(documentSnapshot.getString("email"));
                    mobileNumberTextView.setText(documentSnapshot.getString("mobileNumber"));
                });
    }
}
