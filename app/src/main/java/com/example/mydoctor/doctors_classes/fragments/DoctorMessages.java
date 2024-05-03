package com.example.mydoctor.doctors_classes.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.example.mydoctor.data_structures.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;


public class DoctorMessages extends Fragment {
    User user;
    private LinearLayout container;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("USER");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_doctor_messages, container, false);
        this.container = view.findViewById(R.id.container);
        assert currentUser != null;
        fetchRequestsAndDisplay();
        return view;
    }

    private void fetchRequestsAndDisplay(){
        if (currentUser == null) return;
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading children...");
        progressDialog.show();
        db.collection("requests").whereEqualTo("doctorId",currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if(task.isSuccessful()){
                        for(QueryDocumentSnapshot document: task.getResult()){
                            String childId = document.getString("childId");
                            String status = document.getString("status");
                            if(childId != null && status != null){
                                if(status.equals("pending")){
                                    fetchUserAndAddChildCard(childId,status);
                                }
                            }
                        }
                    }
                });
    }
    private void fetchUserAndAddChildCard(String childId, String status){
        db.collection("users").whereEqualTo("roll","Patient").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                        String patientId = documentSnapshot.getId();
                        db.collection("users").document(patientId).collection("children").document(childId)
                                .get().addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        addChildCard(documentSnapshot2, status, patientId);
                                    }
                                });
                    }
                });
    }
    private void addChildCard(DocumentSnapshot child, String status, String patientId){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.patients_card_view_layout, this.container, false);
        Map<String, Object> user = child.getData();
        assert user != null;
        String fullName = (String) user.get("fullName");
        TextView fullNameTextView = cardView.findViewById(R.id.fullName);
        fullNameTextView.setText(fullName);
        TextView addPatient = cardView.findViewById(R.id.addPatientTextView);
        TextView deletePatient = cardView.findViewById(R.id.deleteTextView);
        this.container.addView(cardView);

        if(user.containsKey("imageUri")) {
            String imageUrl = (String) user.get("imageUri");
            ImageView childImageView = cardView.findViewById(R.id.imageViewChild); // Replace with your ImageView ID
            Glide.with(this) // Or use getContext() if 'this' doesn't work
                    .load(imageUrl)
                    .into(childImageView);
        }

        addPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the message and title for the dialog
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to add this child?");

                // Add a positive button to the dialog for "Yes"
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Yes button
                        db.collection("requests")
                                .whereEqualTo("childId", child.getId())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.exists()) {
                                            String documentId = documentSnapshot.getId();
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("status", "added");
                                            updates.put("isShowed",false);
                                            db.collection("requests").document(documentId)
                                                    .update(updates)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(getActivity(), "Patient successfully added", Toast.LENGTH_SHORT).show();
                                                        Log.d("UpdateStatus", "DocumentSnapshot successfully updated!");
                                                    })
                                                    .addOnFailureListener(e -> Log.w("UpdateStatus", "Error updating document", e));
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.w("QueryError", "Error getting documents: ", e));
                    }
                });

                // Add a negative button to the dialog for "No"
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the No button
                        // Do nothing
                    }
                });

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        deletePatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog.Builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                // Set the message and title for the dialog
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to delete this patient?");

                // Add a positive button to the dialog for "Yes"
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Yes button, proceed with deletion
                        db.collection("requests")
                                .whereEqualTo("childId", child.getId())
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                        if (documentSnapshot.exists()) {
                                            String documentId = documentSnapshot.getId();
                                            Map<String, Object> updates = new HashMap<>();
                                            updates.put("status", "Deleted");
                                            db.collection("requests").document(documentId)
                                                    .update(updates)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Toast.makeText(getActivity(), "Patient successfully deleted", Toast.LENGTH_SHORT).show();
                                                        Log.d("UpdateStatus", "DocumentSnapshot successfully updated!");
                                                    })
                                                    .addOnFailureListener(e -> Log.w("UpdateStatus", "Error updating document", e));
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> Log.w("QueryError", "Error getting documents: ", e));
                        db.collection("users").whereEqualTo("roll","Patient").get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                                        String patientId = documentSnapshot.getId();
                                        db.collection("users").document(patientId).collection("children").document(child.getId())
                                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(getActivity(), "Document successfully deleted!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity(), "Error deleting document", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the No button, do nothing
                    }
                });

                // Create and show the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PatientData anotherFragment = new PatientData();
                anotherFragment.setPatientChildData(user,patientId);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });

    }

}