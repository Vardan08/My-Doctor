package com.example.mydoctor.doctors_classes.fragments;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.example.mydoctor.VisitDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Map;


public class DoctorQuestionnaire extends Fragment {
    private String childId;
    private String patientId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Spinner receiving;
    ArrayList receivingArrayList;
    ArrayAdapter receivingAdapter;
    private LinearLayout container;

    public DoctorQuestionnaire(){}
    public void setChildIdAndPatientId(String childId, String patientId) {
        this.childId = childId;
        this.patientId = patientId;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_doctors_questionnaire, container, false);
        this.container = view.findViewById(R.id.container);
//        receiving = view.findViewById(R.id.receiving);
//        receivingArrayList = new ArrayList();
//        receivingArrayList.add("is received?");
//        receivingArrayList.add("received");
//        receivingArrayList.add("don't received");
//        receivingAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, receivingArrayList);
//        receivingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
//        receiving.setAdapter((SpinnerAdapter) receivingAdapter);
        TableLayout tableLayout = view.findViewById(R.id.tableLayout);
        tableLayout.setStretchAllColumns(true);
        tableLayout.removeAllViews(); // Clear previous data
        db.collection("users").document(patientId).collection("children").document(childId).
            get().addOnSuccessListener(documentSnapshot -> {
                    String childName = documentSnapshot.getString("fullName");
                    TextView titleTextView = view.findViewById(R.id.titleTextView);
                    if (childName != null && !childName.isEmpty()) {
                        titleTextView.setText(childName + " Questionnaire");
                    } else {
                        titleTextView.setText("Patient Questionnaire");
                    }
                    db.collection("users")
                            .document(patientId)
                            .collection("children")
                            .document(childId)
                            .collection("vaccines")
                            .get().addOnCompleteListener(task -> {
                               if(task.isSuccessful()){
                                   for(DocumentSnapshot document : task.getResult()){
                                       String vaccineName = document.getString("vaccine");
                                       Boolean vaccineStatus = document.getBoolean("status");

                                       // Создаем новый TableRow
                                       TableRow row = new TableRow(getContext());
                                       row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                       // TextView для названия вакцины
                                       TextView nameTextView = new TextView(getContext());
                                       nameTextView.setText(vaccineName);
                                       nameTextView.setTypeface(null, Typeface.BOLD);
                                       nameTextView.setPadding(5, 15, 5, 15);
                                       TableRow.LayoutParams params1 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                       nameTextView.setLayoutParams(params1);
                                       row.addView(nameTextView);

                                       // Spinner для выбора статуса вакцины
                                       Spinner statusSpinner = new Spinner(getContext());
                                       ArrayList<String> options = new ArrayList<>();
                                       // Определяем текущий статус и задаем варианты для спиннера
                                       String currentStatus = vaccineStatus ? "receive" : "don't receive";
                                       options.add(currentStatus);
                                       options.add(!vaccineStatus ? "receive" : "don't receive");
                                       ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, options);
                                       statusSpinner.setAdapter(spinnerAdapter);
                                       TableRow.LayoutParams params2 = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                                       statusSpinner.setLayoutParams(params2);
                                       row.addView(statusSpinner);
                                       statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                           @Override
                                           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                               // Переключаем статус
                                               boolean newStatus = position == 0 ? vaccineStatus : !vaccineStatus;
                                               if (newStatus != vaccineStatus) { // Обновляем только если статус изменился
                                                   db.collection("users").document(patientId).collection("children").document(childId)
                                                           .collection("vaccines").document(document.getId())
                                                           .update("status", newStatus)
                                                           .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Status updated.", Toast.LENGTH_SHORT).show())
                                                           .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update status.", Toast.LENGTH_SHORT).show());
                                               }
                                           }

                                           @Override
                                           public void onNothingSelected(AdapterView<?> parent) {
                                               // Не делаем ничего, если не выбрано ничего нового
                                           }
                                       });

                                       // Добавляем TableRow в TableLayout
                                       tableLayout.addView(row);
                                   }
                               }else{
                                   Toast.makeText(getContext(), "Failed to load vaccine data.", Toast.LENGTH_SHORT).show();
                               }
                            });
                    assert currentUser != null;
                    db.collection("visits").whereEqualTo("selectedChildId",childId).whereEqualTo("doctorId",currentUser.getUid()).whereEqualTo("meet","We met").get()
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
        if(user != null && user.get("selectedChildId") != null){
            String doctorId = (String) user.get("doctorId");
            String childId = (String) user.get("selectedChildId");
            String date = (String) user.get("date");
            String selectedTime = (String) user.get("selectedTime");
            assert currentUser != null;
            addVisitCard(doctorId,childId,date,selectedTime);
        }

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