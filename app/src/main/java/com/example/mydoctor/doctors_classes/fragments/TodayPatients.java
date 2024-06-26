package com.example.mydoctor.doctors_classes.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TodayPatients extends Fragment {

    private ArrayList<String> spinnerArrList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinearLayout container;
    private final List<DocumentSnapshot> patientSnapshots = new ArrayList<>();
    private SearchView searchView;
    private List<DocumentSnapshot> allPatientSnapshots = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain the fragment instance across configuration changes
//        if (getArguments() != null) {
//            User user = getArguments().getParcelable("USER");
//        }

        if (spinnerArrList == null) {
            spinnerArrList = new ArrayList<>();
            spinnerArrList.add("Choose filter");
            spinnerArrList.add("Visits");
            spinnerArrList.add("All Patients");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_patients, container, false);
        Spinner spinner = view.findViewById(R.id.spinner2);
        searchView = view.findViewById(R.id.search_view);
        TextView calendarDataTextView = view.findViewById(R.id.calendarDataTextView);
        calendarDataTextView.setVisibility(View.GONE);

        // Initialize ArrayAdapter with default selection and apply it
        ArrayAdapter<String> spinnerArrAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerArrList);
        spinnerArrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrAdapter);
        spinner.setSelection(0, false); // Set "Choose filter" as default

        this.container = view.findViewById(R.id.container);
        this.container.removeAllViews();  // Clear all views whenever view is created

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals("All Patients")) {
                    calendarDataTextView.setVisibility(View.GONE);
                    calendarDataTextView.setText("Calendar Data");
                    allPatients();
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String newText) {
                            if (newText.isEmpty()) {
                                allPatients();
                            } else {
                                filterAllPatients(newText);
                            }
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String query) {
                            if (query.isEmpty()) {
                                if(calendarDataTextView.getText().toString().equals("Calendar Data")){
                                    allPatients();
                                }
//                                else{
//                                    allPatients(calendarDataTextView.getText().toString());
//                                }

                            }
                            return true;
                        }
                    });
                }else if(parent.getItemAtPosition(position).toString().equals("Visits")){
                    calendarDataTextView.setText("Calendar Data");
                    visits();
                    calendarDataTextView.setVisibility(View.VISIBLE);
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
                                    visits(formattedDate);
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
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (query.isEmpty()) {
                                if(calendarDataTextView.getText().toString().equals("Calendar Data")){
                                    visits();
                                }else{
                                    visits(calendarDataTextView.getText().toString());
                                }

                            } else {
                                if(!calendarDataTextView.getText().toString().equals("Calendar Data")){
                                    searchPatients(query,calendarDataTextView.getText().toString());
                                }else{
                                    searchPatients(query);
                                }
                            }
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            if (newText.isEmpty() && calendarDataTextView.getText().toString().equals("Calendar Data")) {
                                visits();
                            }else if(newText.isEmpty() && !calendarDataTextView.getText().toString().equals("Calendar Data")){
                                visits(calendarDataTextView.getText().toString());
                            }
                            return true;
                        }
                    });
                } else {
                    TodayPatients.this.container.removeAllViews();// Clear views if "Choose filter" or any non-All Patients item is selected
                    calendarDataTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        clearSearchView();
        return view;
    }
    private void searchPatients(String query) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;

        String lowerCaseQuery = query.toLowerCase();

        db.collection("users")
                .whereEqualTo("roll", "Patient")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        container.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String patientId = document.getId();
                            db.collection("users")
                                    .document(patientId)
                                    .collection("children")
                                    .whereEqualTo("Doctor", currentUser.getUid())
                                    .get()
                                    .addOnCompleteListener(childTask -> {
                                        if (childTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot childDocument : childTask.getResult()) {
                                                String fullName = childDocument.getString("fullName");
                                                if (fullName != null && fullName.toLowerCase().contains(lowerCaseQuery)) {
                                                    db.collection("visits")
                                                            .whereEqualTo("selectedChildId", childDocument.getId())
                                                            .get()
                                                            .addOnCompleteListener(visitTask -> {
                                                                if (visitTask.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot visitDocument : visitTask.getResult()) {
                                                                        addVisitCard(
                                                                                childDocument,
                                                                                visitDocument.getString("date"),
                                                                                visitDocument.getString("selectedTime"),
                                                                                visitDocument.getString("userInput"),
                                                                                patientId,
                                                                                visitDocument.getString("meet")
                                                                        );
                                                                    }
                                                                } else {
                                                                    Log.w("Firestore", "Error getting visit documents.", visitTask.getException());
                                                                }
                                                            });
                                                }
                                            }
                                        } else {
                                            Log.w("Firestore", "Error getting child documents.", childTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.w("Firestore", "Error getting user documents.", task.getException());
                    }
                });
    }
    private void searchPatients(String query, String selectedDate) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        assert currentUser != null;

        String lowerCaseQuery = query.toLowerCase();

        db.collection("users")
                .whereEqualTo("roll", "Patient")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        container.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String patientId = document.getId();
                            db.collection("users")
                                    .document(patientId)
                                    .collection("children")
                                    .whereEqualTo("Doctor", currentUser.getUid())
                                    .get()
                                    .addOnCompleteListener(childTask -> {
                                        if (childTask.isSuccessful()) {
                                            for (QueryDocumentSnapshot childDocument : childTask.getResult()) {
                                                String fullName = childDocument.getString("fullName");
                                                if (fullName != null && fullName.toLowerCase().contains(lowerCaseQuery)) {
                                                    db.collection("visits")
                                                            .whereEqualTo("selectedChildId", childDocument.getId())
                                                            .whereEqualTo("date", selectedDate)
                                                            .get()
                                                            .addOnCompleteListener(visitTask -> {
                                                                if (visitTask.isSuccessful()) {
                                                                    for (QueryDocumentSnapshot visitDocument : visitTask.getResult()) {
                                                                        addVisitCard(
                                                                                childDocument,
                                                                                visitDocument.getString("date"),
                                                                                visitDocument.getString("selectedTime"),
                                                                                visitDocument.getString("userInput"),
                                                                                patientId,
                                                                                visitDocument.getString("meet")
                                                                        );
                                                                    }
                                                                } else {
                                                                    Log.w("Firestore", "Error getting visit documents.", visitTask.getException());
                                                                }
                                                            });
                                                }
                                            }
                                        } else {
                                            Log.w("Firestore", "Error getting child documents.", childTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.w("Firestore", "Error getting user documents.", task.getException());
                    }
                });
    }



    private void clearSearchView() {
        if (searchView != null) {
            searchView.setQuery("", false);
            searchView.clearFocus();
        }
    }
    private void visits() {
        if (currentUser != null) {
            this.container.removeAllViews();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setTimeZone(TimeZone.getDefault()); // Consider the user's time zone
            String todayString = df.format(new Date());

            Toast.makeText(getContext(), "Fetching visits for today and future: " + todayString, Toast.LENGTH_SHORT).show();

            db.collection("visits")
                    .whereEqualTo("doctorId", currentUser.getUid())
                    .whereGreaterThanOrEqualTo("date", todayString) // Filter to include today and future dates
                    .orderBy("date") // Order the results by date
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            patientSnapshots.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String childId = document.getString("selectedChildId");
                                String date = document.getString("date");
                                String selectedTime = document.getString("selectedTime");
                                String userInput = document.getString("userInput");
                                String meet = document.getString("meet");
                                fetchChildrenAndAddVisitCard(childId, date, selectedTime, userInput, meet);
                            }
                        } else {
                            Log.e("VisitsActivity", "Failed to load visits", task.getException());
                            Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void visits(String date){
        if(currentUser != null){
            this.container.removeAllViews();
            db.collection("visits").whereEqualTo("doctorId",currentUser.getUid()).whereEqualTo("date",date)
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            patientSnapshots.clear();
                            for(QueryDocumentSnapshot document : task.getResult()){
                                String childId = document.getString("selectedChildId");
                                String selectedTime = document.getString("selectedTime");
                                String userInput = document.getString("userInput");
                                String meet = document.getString("meet");
                                fetchChildrenAndAddVisitCard(childId,date,selectedTime,userInput,meet);
                            }
                        }else{
                            Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    private void fetchChildrenAndAddVisitCard(String childId, String date, String selectedTime, String userInput, String meet) {
        db.collection("users")
                .whereEqualTo("roll", "Patient")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String patientId = documentSnapshot.getId();
                        db.collection("users")
                                .document(patientId)
                                .collection("children")
                                .document(childId)
                                .get()
                                .addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        patientSnapshots.add(documentSnapshot2);
                                        addVisitCard(documentSnapshot2, date, selectedTime, userInput, patientId, meet);
                                    }
                                });
                    }
                });
    }


    @SuppressLint("SetTextI18n")
    private void addVisitCard(DocumentSnapshot child, String date, String selectedTime, String userInput, String patientId, String meet) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);
        Map<String, Object> user = child.getData();
        assert currentUser != null;
        TextView cildFullNameTextView = cardView.findViewById(R.id.textViewFullName);
        TextView weMet = cardView.findViewById(R.id.status);
        weMet.setVisibility(View.VISIBLE);
        weMet.setText(meet);
        assert user != null;
        cildFullNameTextView.setText((String) user.get("fullName"));
        TextView selectedTimeTextView = cardView.findViewById(R.id.textViewLocation);
        selectedTimeTextView.setText(selectedTime);
        TextView dateTextView = cardView.findViewById(R.id.textViewDOB);
        dateTextView.setText(date);
        TextView description = cardView.findViewById(R.id.textViewDoctor);
        description.setText(userInput);
        TextView weMeet = cardView.findViewById(R.id.textViewCancel);

        weMeet.setText("We have met");
        weMeet.setVisibility(View.VISIBLE);
        if(meet.equals("We met")){
            weMeet.setVisibility(View.GONE);
        }
        weMeet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an AlertDialog builder
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // Set title for the dialog
                builder.setTitle("Meeting Description");

                // Create an EditText to allow user input
                final EditText input = new EditText(getActivity());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set positive button for the dialog
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retrieve the entered description
                        String description = input.getText().toString().trim();
                        if(description.isEmpty()){
                            Toast.makeText(getActivity(), "Please write description", Toast.LENGTH_SHORT).show();
                        }else {
                            Map<String, Object> meetData = new HashMap<>();
                            meetData.put("date", date);
                            meetData.put("description", description);
                            meetData.put("selectedTime", selectedTime);

                            db.collection("users")
                                    .document(patientId)
                                    .collection("children")
                                    .document(child.getId())
                                    .collection("meetings")
                                    .add(meetData)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(getActivity(), "You met patient successfully", Toast.LENGTH_SHORT).show();
                                        Log.d("Success", "DocumentSnapshot added with ID: " + documentReference.getId());
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Failed to meet patient: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        Log.e("Failure", "Error adding document", e);
                                    });

                            db.collection("visits")
                                    .whereEqualTo("selectedChildId",child.getId())
                                    .whereEqualTo("date",date)
                                    .get().addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                                db.collection("visits").document(documentSnapshot.getId()).update("meet","We met")
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("UpdateSuccess", "Document successfully updated");
                                                            weMet.setText("We met");
                                                            weMeet.setVisibility(View.GONE);
                                                        }).addOnFailureListener(e -> Log.w("UpdateFailure", "Error updating document", e));
                                            }
                                        }
                                    });
                        }
                    }
                });

                // Set negative button for the dialog
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel(); // Close the dialog
                    }
                });

                // Show the dialog
                builder.show();
            }
        });
        if (user.containsKey("imageUri")) {
            Glide.with(this).load((String) user.get("imageUri")).into((ImageView) cardView.findViewById(R.id.imageViewChild));
        }
        this.container.addView(cardView);
        cardView.setOnClickListener(v -> {
            PatientData anotherFragment = new PatientData();
            anotherFragment.setPatientChildData(user, patientId,child.getId());
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);
            clearSearchView();
            fragmentTransaction.commit();
        });
    }


    private void allPatients() {
        if (currentUser != null) {
            this.container.removeAllViews();  // Clear existing views to prevent duplication
            db.collection("requests").whereEqualTo("doctorId", currentUser.getUid())
                    .whereEqualTo("status","added").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            allPatientSnapshots.clear(); // Clear existing patient snapshots
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String childId = document.getString("childId");
                                fetchUserAndAddChildCard(childId);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchUserAndAddChildCard(String childId) {
        db.collection("users").whereEqualTo("roll", "Patient").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String patientId = documentSnapshot.getId();
                        db.collection("users").document(patientId).collection("children").document(childId)
                                .get().addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        allPatientSnapshots.add(documentSnapshot2);
                                        addAllChildCard(documentSnapshot2, patientId);
                                    }
                                });
                    }
                });
    }

    private void filterAllPatients(String query) {
        this.container.removeAllViews(); // Clear existing views
        String lowerCaseQuery = query.toLowerCase();

        for (DocumentSnapshot document : allPatientSnapshots) {
            String fullName = document.getString("fullName");
            if (fullName != null && fullName.toLowerCase().contains(lowerCaseQuery)) {
                String patientId = document.getReference().getParent().getParent().getId();
                addAllChildCard(document, patientId);
            }
        }
    }
    private void addAllChildCard(DocumentSnapshot child, String patientId) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);
        Map<String, Object> user = child.getData();
        assert user != null;
        ((TextView) cardView.findViewById(R.id.textViewFullName)).setText((String) user.get("fullName"));
        ((TextView) cardView.findViewById(R.id.textViewLocation)).setText((String) user.get("location"));
        ((TextView) cardView.findViewById(R.id.textViewDOB)).setText((String) user.get("dob"));
        ((TextView) cardView.findViewById(R.id.textViewDoctor)).setText((String) user.get("birthCertDetails"));
        if (user.containsKey("imageUri")) {
            Glide.with(this).load((String) user.get("imageUri")).into((ImageView) cardView.findViewById(R.id.imageViewChild));
        }
        this.container.addView(cardView);
        cardView.setOnClickListener(v -> {
            PatientData anotherFragment = new PatientData();
            anotherFragment.setPatientChildData(user, patientId, child.getId());
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }


//    private void allChildrenFilter(String text) {
//        container.removeAllViews(); // Clear the existing views/cards
//        for (DocumentSnapshot patientSnapshot : patientSnapshots) { // Iterate through the snapshot list
//            String fullName = patientSnapshot.getString("fullName");
//            if (fullName != null && fullName.toLowerCase().contains(text.toLowerCase())) {
//                addAllChildCard(patientSnapshot, patientId); // Add the card if it matches the filter
//            }
//        }
//    }
}