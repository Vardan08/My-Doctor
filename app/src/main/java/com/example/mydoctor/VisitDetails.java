package com.example.mydoctor;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class VisitDetails extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    String doctorId;
    String childId;
    String selectedTime;
    String date;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            doctorId = getArguments().getString("doctorId");
            childId = getArguments().getString("childId");
            date = getArguments().getString("date");
            selectedTime = getArguments().getString("selectedTime");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visit_details, container, false);
        assert currentUser != null;
        db.collection("users").whereEqualTo("roll", "Patient").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String patientId = documentSnapshot.getId();
                        db.collection("users").document(patientId).collection("children").document(childId)
                                .get().addOnSuccessListener(documentSnapshot1 -> {
                                    if(documentSnapshot1 != null){
                                        String fullName = documentSnapshot1.getString("fullName");
                                        if (fullName != null) {
                                            TextView childNameTextView = view.findViewById(R.id.titleTextView);
                                            childNameTextView.setText(fullName + "'s " + "visit details");
                                            db.collection("users")
                                            .document(doctorId)
                                            .get()
                                            .addOnSuccessListener(documentSnapshot2 -> {
                                                Map<String,Object> doctor = documentSnapshot2.getData();
                                                ImageView doctorImage = view.findViewById(R.id.profilePhoto);
                                                assert doctor != null;
                                                if (doctor.containsKey("profileImageUrl")) {
                                                    String imageUri = (String) doctor.get("profileImageUrl");
                                                    Glide.with(this).load(imageUri).into(doctorImage);
                                                }
                                                TextView doctorNameTextView = view.findViewById(R.id.textView3);
                                                doctorNameTextView.setText(documentSnapshot2.getString("fullName"));
                                                db.collection("users")
                                                        .document(patientId)
                                                        .collection("children")
                                                        .document(childId)
                                                        .collection("meetings")
                                                        .whereEqualTo("date",date)
                                                        .whereEqualTo("selectedTime",selectedTime)
                                                        .get()
                                                        .addOnCompleteListener(task -> {
                                                            if(task.isSuccessful()){
                                                                for(DocumentSnapshot documentSnapshot3 : task.getResult()){
                                                                    if(documentSnapshot3 != null && documentSnapshot3.getString("description") != null){
                                                                        TextView dateTextView = view.findViewById(R.id.textDate);
                                                                        dateTextView.setText(documentSnapshot3.getString("date"));
                                                                        TextView timeTextView = view.findViewById(R.id.textTime);
                                                                        timeTextView.setText(documentSnapshot3.getString("selectedTime"));
                                                                        TextView descriptionTextView = view.findViewById(R.id.textDescription);
                                                                        descriptionTextView.setText(documentSnapshot3.getString("description"));
                                                                    }
                                                                }
                                                            }
                                                        });
                                            });
                                        }
                                    }
                                });
                    }
                });
//        db.collection("users")
//                .document(currentUser.getUid())
//                .collection("children")
//                .document(childId)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    Map<String,Object> child = documentSnapshot.getData();
//                    assert child != null;
////                    String fullName = (String) child.get("fullName");
////                    TextView childNameTextView = view.findViewById(R.id.titleTextView);
////                    childNameTextView.setText(fullName + "'s " + "visit details");
//                    db.collection("users")
//                            .document(doctorId)
//                            .get()
//                            .addOnSuccessListener(documentSnapshot1 -> {
//                                Map<String,Object> doctor = documentSnapshot1.getData();
//                                ImageView doctorImage = view.findViewById(R.id.profilePhoto);
//                                assert doctor != null;
//                                if (doctor.containsKey("profileImageUrl")) {
//                                    String imageUri = (String) doctor.get("profileImageUrl");
//                                    Glide.with(this).load(imageUri).into(doctorImage);
//                                }
//                                TextView doctorNameTextView = view.findViewById(R.id.textView3);
//                                doctorNameTextView.setText(documentSnapshot1.getString("fullName"));
//                            });
//                });
        return view;
    }
}