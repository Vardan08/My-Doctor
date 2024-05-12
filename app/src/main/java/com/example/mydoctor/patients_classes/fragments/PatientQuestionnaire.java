package com.example.mydoctor.patients_classes.fragments;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.example.mydoctor.VisitDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class PatientQuestionnaire extends Fragment {

    private String childId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinearLayout container;

    public PatientQuestionnaire() {
        // Required empty public constructor
    }

    public void setChildId(String childId) {
        this.childId = childId;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_questionnaire, container, false);
        this.container = view.findViewById(R.id.container);
        assert currentUser != null;
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);
        tableLayout.setStretchAllColumns(true);
        tableLayout.removeAllViews(); // Clear previous data

        db.collection("users").document(currentUser.getUid()).collection("children").document(childId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    String childName = documentSnapshot.getString("fullName");
                    TextView titleTextView = view.findViewById(R.id.titleTextView);
                    if (childName != null && !childName.isEmpty()) {
                        titleTextView.setText(childName + " Questionnaire");
                    } else {
                        titleTextView.setText("Patient Questionnaire");
                    }

                    db.collection("users").document(currentUser.getUid()).collection("children").document(childId).collection("vaccines")
                            .get().addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    for(DocumentSnapshot document : task.getResult()){
                                        String vaccineName = document.getString("vaccine");
                                        Boolean vaccineStatus = document.getBoolean("status");

                                        TableRow row = new TableRow(getContext());
                                        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                        TextView nameTextView = new TextView(getContext());
                                        nameTextView.setText(vaccineName);
                                        nameTextView.setTypeface(null, Typeface.BOLD);
                                        nameTextView.setPadding(5, 15, 5, 15);
                                        TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                        nameTextView.setLayoutParams(params1);
                                        row.addView(nameTextView);

                                        TextView statusTextView = new TextView(getContext());
                                        statusTextView.setText(vaccineStatus ? "receive" : "don't receive");
                                        statusTextView.setPadding(5, 15, 5, 15);
                                        TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                        statusTextView.setLayoutParams(params2);
                                        row.addView(statusTextView);

                                        tableLayout.addView(row);
                                    }
                                } else {
                                    Toast.makeText(getContext(), "Failed to load vaccine data.", Toast.LENGTH_SHORT).show();
                                }
                            });
                    db.collection("visits").whereEqualTo("selectedChildId",childId).whereEqualTo("meet","We met").get()
                            .addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    for(DocumentSnapshot documentSnapshot1 : task.getResult()){
                                        fetchDoctorAndChildIds(documentSnapshot1);
                                    }
                                }
                            });
                });

        return view;
    }
    private void fetchDoctorAndChildIds(DocumentSnapshot documentSnapshot){
        Map<String, Object> user = documentSnapshot.getData();
        assert user != null;
        String doctorId = (String) user.get("doctorId");
        String childId = (String) user.get("selectedChildId");
        String date = (String) user.get("date");
        String selectedTime = (String) user.get("selectedTime");
        Toast.makeText(getActivity(), date, Toast.LENGTH_SHORT).show();
       addVisitCard(doctorId,childId,date,selectedTime);
    }
    @SuppressLint("SetTextI18n")
    private void addVisitCard(String doctorId, String childId, String date, String selectedTime){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.questionnaire_visits_card_view, this.container, false);
        db.collection("users").document(doctorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, Object> user = documentSnapshot.getData();
                    assert user != null;
                    TextView fullNameTextView = cardView.findViewById(R.id.fullName);
                    fullNameTextView.setText((String) user.get("fullName"));
                    TextView timeTextView = cardView.findViewById(R.id.timeTextView);
                    timeTextView.setText(date);
                    ImageView doctorImage = cardView.findViewById(R.id.imageViewChild);
                    if (user.containsKey("profileImageUrl")) {
                        String imageUri = (String) user.get("profileImageUrl");
                        Glide.with(this).load(imageUri).into(doctorImage);
                    }
                });

//        assert currentUser != null;
//        db.collection("users").document(currentUser.getUid()).collection("children").document(childId).collection("meetings")
//                .whereEqualTo("date",date).get().addOnCompleteListener(task -> {
//                    for(DocumentSnapshot documentSnapshot : task.getResult()){
//                        documentSnapshot.getString("description");
//                    }
//                });
        this.container.addView(cardView);
        TextView seeAllDetails = cardView.findViewById(R.id.seeAllDetails);
        seeAllDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VisitDetails anotherFragment = new VisitDetails();
                Bundle args = new Bundle();
                args.putString("doctorId",doctorId);
                args.putString("childId",childId);
                args.putString("date",date);
                args.putString("selectedTime",selectedTime);
                anotherFragment.setArguments(args);
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
            }
        });
    }
}
