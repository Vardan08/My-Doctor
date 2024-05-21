package com.example.mydoctor.doctors_classes.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.example.mydoctor.data_structures.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class HistoryOfVisits extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinearLayout container;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain the fragment instance across configuration changes
        if (getArguments() != null) {
            User user = getArguments().getParcelable("USER");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history_of_visits, container, false);
        this.container = view.findViewById(R.id.container);
        this.container.removeAllViews();
        TextView calendarDataTextView = view.findViewById(R.id.calendarDataTextView);
        calendarDataTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current date from a Calendar instance
                final Calendar selectedDateTime = Calendar.getInstance();
                int year = selectedDateTime.get(Calendar.YEAR);
                int month = selectedDateTime.get(Calendar.MONTH);
                int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

                // Create a DatePickerDialog
                DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonthOfYear, int selectedDayOfMonth) {
                        // This method is called when the user selects a date and clicks "OK"
                        selectedDateTime.set(Calendar.YEAR, selectedYear);
                        selectedDateTime.set(Calendar.MONTH, selectedMonthOfYear);
                        selectedDateTime.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);

                        // Format the date to a string or any other formatting or processing you need
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedDate = dateFormat.format(selectedDateTime.getTime());

                        // Here, you can do whatever you need with the selected date
                        // For example, displaying it in a Toast or handling more business logic
                        Toast.makeText(requireContext(), "Selected date: " + formattedDate, Toast.LENGTH_LONG).show();
                        calendarDataTextView.setText(formattedDate);
                        getVisitsUsingDate(formattedDate);
                    }
                }, year, month, day);

                // Optional: If you want to do something when the dialog is dismissed (either by selecting a date or cancelling)
                datePickerDialog.setOnDismissListener(dialog -> {
                    // Code here is executed when the DatePickerDialog is dismissed
                });

                // Show the DatePickerDialog
                datePickerDialog.show();
            }
        });
        getVisits();
        return view;
    }
    private void getVisits() {
        if (currentUser != null) {
            this.container.removeAllViews();
            db.collection("visits")
                    .whereEqualTo("doctorId", currentUser.getUid())
                    .orderBy("date")  // Ensure correct ordering by date
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                fetchChildrenAndAddVisitCard(document);
                            }
                        } else {
                            Log.e("VisitsActivity", "Failed to load visits", task.getException());
                            Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void getVisitsUsingDate(String date) {
        if (currentUser != null) {
            this.container.removeAllViews();
            db.collection("visits")
                    .whereEqualTo("doctorId", currentUser.getUid())
                    .whereEqualTo("date", date)
                    .orderBy("date")  // Ensure correct ordering by date
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                fetchChildrenAndAddVisitCard(document);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchChildrenAndAddVisitCard(QueryDocumentSnapshot visitDocument) {
        String childId = visitDocument.getString("selectedChildId");
        db.collection("users")
                .whereEqualTo("roll", "Patient")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String patientId = documentSnapshot.getId();
                        assert childId != null;
                        db.collection("users")
                                .document(patientId)
                                .collection("children")
                                .document(childId)
                                .get()
                                .addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        addVisitCard(documentSnapshot2, visitDocument ,patientId);
                                    }
                                });
                    }
                });
    }
    private void addVisitCard(DocumentSnapshot childDocument, DocumentSnapshot visitDocument, String patientId) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);
        Map<String, Object> user = childDocument.getData();
        Map<String, Object> visit = visitDocument.getData();
        assert currentUser != null;
        TextView cildFullNameTextView = cardView.findViewById(R.id.textViewFullName);
        TextView weMet = cardView.findViewById(R.id.status);
        weMet.setVisibility(View.VISIBLE);
        assert visit != null;
        weMet.setText(Objects.requireNonNull(visit.get("meet")).toString());
        assert user != null;
        cildFullNameTextView.setText((String) user.get("fullName"));
        TextView selectedTimeTextView = cardView.findViewById(R.id.textViewLocation);
        selectedTimeTextView.setText(Objects.requireNonNull(visit.get("selectedTime")).toString());
        TextView dateTextView = cardView.findViewById(R.id.textViewDOB);
        dateTextView.setText(Objects.requireNonNull(visit.get("date")).toString());
        TextView description = cardView.findViewById(R.id.textViewDoctor);
        description.setText(Objects.requireNonNull(visit.get("userInput")).toString());
        if (user.containsKey("imageUri")) {
            Glide.with(this).load((String) user.get("imageUri")).into((ImageView) cardView.findViewById(R.id.imageViewChild));
        }
        this.container.addView(cardView);
        cardView.setOnClickListener(v -> {
            PatientData anotherFragment = new PatientData();
            anotherFragment.setPatientChildData(user, patientId,childDocument.getId());
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }
}