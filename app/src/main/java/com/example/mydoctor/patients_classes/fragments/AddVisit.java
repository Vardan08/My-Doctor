package com.example.mydoctor.patients_classes.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.example.mydoctor.data_structures.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AddVisit extends Fragment {
    private Button addEventButton;
    private Calendar selectedDateTime = Calendar.getInstance();
    private User user;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinearLayout container;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("USER");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_visit, container, false);
        this.container = rootView.findViewById(R.id.container);
        addEventButton = rootView.findViewById(R.id.addEventButton);
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChildSelectionDialog();
            }
        });

        if(currentUser != null){
            fetchVisitsAndDisplay();
        }else {
            Toast.makeText(getActivity(), "Current user is null", Toast.LENGTH_SHORT).show();
        }

        return rootView;
    }

    private void showChildSelectionDialog() {
        assert currentUser != null;
        List<DocumentSnapshot> childNames = new ArrayList<>();
        AtomicInteger remainingRequests = new AtomicInteger(); // Counter for pending requests

        db.collection("users").document(currentUser.getUid()).collection("children").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String doctorId = document.getString("Doctor");
                            if (doctorId != null) {
                                String childId = document.getId();
                                remainingRequests.incrementAndGet(); // Increment for each child request started
                                db.collection("requests").whereEqualTo("childId", childId).get().addOnCompleteListener(task2 -> {
                                    try {
                                        if (task2.isSuccessful() && task2.getResult() != null) {
                                            for (QueryDocumentSnapshot document2 : task2.getResult()) {
                                                String status = document2.getString("status");
                                                if (status != null && status.equals("added")) {
                                                    String childName = document.getString("fullName");
                                                    if (childName != null) {
                                                        childNames.add(document);
                                                    }
                                                }
                                            }
                                        }
                                    } finally {
                                        if (remainingRequests.decrementAndGet() == 0) { // Decrement and check if all requests have finished
                                            if (!childNames.isEmpty()) {
                                                openDialog(childNames);
                                            } else {
                                                Toast.makeText(getActivity(), "No children found", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                        if (remainingRequests.get() == 0) { // Check if no requests were started
                            Toast.makeText(getActivity(), "No children with doctors found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Failed to fetch children", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void openDialog(List<DocumentSnapshot> childSnapshots) {
        assert childSnapshots != null;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // Context should be your Activity
        builder.setTitle("Select a Child");

        // Extracting child names and their IDs from DocumentSnapshots
        List<String> childNames = new ArrayList<>();
        List<String> childIds = new ArrayList<>();
        for (DocumentSnapshot snapshot : childSnapshots) {
            if (snapshot.contains("fullName")) {
                String name = snapshot.getString("fullName");
                if (name != null) {
                    childNames.add(name);
                    childIds.add(snapshot.getId()); // Store the document ID
                }
            }
        }

        // Convert the list of names to array for the dialog
        CharSequence[] childArray = childNames.toArray(new CharSequence[0]);
        builder.setItems(childArray, (dialog, which) -> {
            // 'which' is the index of the selected item
            String selectedChild = childNames.get(which);
            String selectedChildId = childIds.get(which); // Get the ID of the selected child
            Toast.makeText(getActivity(), "You selected: " + selectedChild + " with ID: " + selectedChildId, Toast.LENGTH_LONG).show();
            showDateTimePicker(selectedChildId);
        });

        builder.setPositiveButton("Close", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showDateTimePicker(String selectedChildId) {
        // Get the current date and time
        final Calendar currentDate = Calendar.getInstance();

        // Get the current date from a Calendar instance for the dialog default
        final Calendar selectedDateTime = (Calendar) currentDate.clone();

        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonthOfYear, int selectedDayOfMonth) {
                // Set the chosen date
                selectedDateTime.set(Calendar.YEAR, selectedYear);
                selectedDateTime.set(Calendar.MONTH, selectedMonthOfYear);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);

                // Set time to beginning of the day for accurate comparison
                selectedDateTime.set(Calendar.HOUR_OF_DAY, 0);
                selectedDateTime.set(Calendar.MINUTE, 0);
                selectedDateTime.set(Calendar.SECOND, 0);
                selectedDateTime.set(Calendar.MILLISECOND, 0);

                // Also reset the time components of the current date for accurate day comparison
                currentDate.set(Calendar.HOUR_OF_DAY, 0);
                currentDate.set(Calendar.MINUTE, 0);
                currentDate.set(Calendar.SECOND, 0);
                currentDate.set(Calendar.MILLISECOND, 0);

                // Compare with current date
                if (!selectedDateTime.before(currentDate)) {
                    // If selected date is today or in the future

                    // Format the date to a string
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String formattedDate = dateFormat.format(selectedDateTime.getTime());

                    // Use the selected date
                    Toast.makeText(requireContext(), "Selected date: " + formattedDate, Toast.LENGTH_LONG).show();
                    getDoctorId(selectedChildId, formattedDate);
                } else {
                    // If the selected date is before today, do not call getDoctorId
                    Toast.makeText(requireContext(), "Selected date is in the past!", Toast.LENGTH_LONG).show();
                }
            }
        }, year, month, day);

        datePickerDialog.setOnDismissListener(dialog -> {
            // Code here is executed when the DatePickerDialog is dismissed
        });

        // Show the DatePickerDialog
        datePickerDialog.show();
    }





    private void getDoctorId(String selectedChildId,String date){
        db.collection("users").whereEqualTo("roll","Patient").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                        String patientId = documentSnapshot.getId();
                        db.collection("users").document(patientId).collection("children").document(selectedChildId)
                                .get().addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        String doctorId = (String) documentSnapshot2.get("Doctor");
                                        takeDoctorHours(selectedChildId,date,doctorId);
                                    }
                                });
                    }
                });
    }

    private void takeDoctorHours(String selectedChildId, String date, String doctorId){
        db.collection("users").document(doctorId).get().addOnSuccessListener(documentSnapshot2 -> {
            if(documentSnapshot2.exists()){
                ArrayList<String> doctorHours = (ArrayList<String>) documentSnapshot2.get("timeSet");
                Toast.makeText(getActivity(), "" + doctorHours, Toast.LENGTH_SHORT).show();
                assert doctorHours != null;

                // Получение текущей даты и времени
                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();

                // Парсинг даты из аргумента, предполагается что формат даты yyyy-MM-dd
                LocalDate selectedDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);

                // Fetch visits to find already chosen times.
                db.collection("visits")
                        .whereEqualTo("date", date)
                        .whereEqualTo("doctorId", doctorId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                ArrayList<String> chosenTimes = new ArrayList<>();
                                for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                    chosenTimes.add(documentSnapshot.getString("selectedTime"));
                                }
                                ArrayList<String> availableHours = new ArrayList<>(doctorHours);
                                availableHours.removeAll(chosenTimes);

                                // Удаление прошедших часов, только если выбранная дата сегодня
                                if(selectedDate.equals(today)) {
                                    availableHours.removeIf(time -> LocalTime.parse(time).isBefore(now));
                                }

                                // Вызов диалога с доступными часами
                                doctorHoursDialog(selectedChildId, date, doctorId, availableHours);
                            } else {
                                Log.e("Firestore Error", task.getException().toString());
                                doctorHoursDialog(selectedChildId, date, doctorId, doctorHours); // fallback to full list
                            }
                        });
            } else {
                Toast.makeText(getActivity(), "No doctor information available.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void doctorHoursDialog(String selectedChildId, String date, String doctorId, ArrayList<String> doctorHours){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a hour");
        CharSequence[] childArray = doctorHours.toArray(new CharSequence[0]);
        builder.setItems(childArray, (dialog, which) -> {
            String selectedTime = doctorHours.get(which);
            userDescription(selectedChildId,date,doctorId,selectedTime);
        });
        builder.setPositiveButton("Close", (dialog, id) -> dialog.dismiss());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void userDescription(String selectedChildId, String date, String doctorId, String selectedTime){
        final EditText input = new EditText(getActivity());

        // Creating the AlertDialog.Builder instance
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Input Description");

        // Setting the EditText as the view of the AlertDialog
        builder.setView(input);

        // Adding the positive button and handling its click event
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String userInput = input.getText().toString();
                saveVisitData(selectedChildId,date,doctorId,selectedTime,userInput);
            }
        });

        // Adding a negative button
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });

        // Creating and showing the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveVisitData(String selectedChildId, String date, String doctorId, String selectedTime, String userInput){
        Map<String, Object> visitData = new HashMap<>();
        visitData.put("selectedChildId",selectedChildId);
        visitData.put("date",date);
        visitData.put("doctorId",doctorId);
        visitData.put("selectedTime",selectedTime);
        visitData.put("userInput",userInput);
        visitData.put("meet", "We haven't met");
        db.collection("visits")
                .add(visitData)
                .addOnSuccessListener(documentReference -> {
                    // Data has been saved successfully!
                    Toast.makeText(getActivity(), "visit add successfully", Toast.LENGTH_SHORT).show();
                    db.collection("users").document(currentUser.getUid()).collection("children").document(selectedChildId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String childFullName = (String) documentSnapshot.get("fullName");
                                String childImageUrl = documentSnapshot.getString("imageUri");
                                addVisitCard(childFullName,selectedChildId,date,doctorId,selectedTime,userInput,documentReference.getId(),childImageUrl,"We haven't met");
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Error get document" + e, Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                    Toast.makeText(getActivity(), "Error adding document: " + e, Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchVisitsAndDisplay() {
        db.collection("users").document(currentUser.getUid()).collection("children")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        for(QueryDocumentSnapshot document : task.getResult()) {
                            String childId = document.getId();
                            String childName = document.getString("fullName");
                            String childImageUrl = document.getString("imageUri");
                            if(childName != null && childId != null) {
                                db.collection("visits").whereEqualTo("selectedChildId", childId).get()
                                        .addOnCompleteListener(task1 -> {
                                            if(task1.isSuccessful()) {
                                                LocalDate today = LocalDate.now();
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                                for(QueryDocumentSnapshot document1 : task1.getResult()) {
                                                    String dateStr = document1.getString("date");
                                                    if (dateStr != null) {
                                                        LocalDate visitDate = LocalDate.parse(dateStr, formatter);
                                                        // Сравнение даты визита с текущей датой
                                                        if (!visitDate.isBefore(today)) {
                                                            String doctorId = document1.getString("doctorId");
                                                            String selectedTime = document1.getString("selectedTime");
                                                            String userInput = document1.getString("userInput");
                                                            String meet = document1.getString("meet");
                                                            addVisitCard(childName, childId, dateStr, doctorId, selectedTime, userInput, document1.getId(), childImageUrl,meet);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void addVisitCard(String childFullName, String selectedChildId, String date, String doctorId, String selectedTime, String userInput, String visitDocumentId,String childImageUrl, String meet){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout , this.container, false);
        assert currentUser != null;
        TextView cildFullNameTextView = cardView.findViewById(R.id.textViewFullName);
        TextView weMet = cardView.findViewById(R.id.status);
        weMet.setVisibility(View.VISIBLE);
        weMet.setText(meet);
        cildFullNameTextView.setText(childFullName);
        ImageView childImageView = cardView.findViewById(R.id.imageViewChild);
        if(childImageUrl != null){
            Glide.with(getActivity())
                    .load(childImageUrl)
                    .into(childImageView);
        }
        TextView selectedTimeTextView = cardView.findViewById(R.id.textViewLocation);
        selectedTimeTextView.setText(selectedTime);
        TextView dateTextView = cardView.findViewById(R.id.textViewDOB);
        dateTextView.setText(date);
        TextView description = cardView.findViewById(R.id.textViewDoctor);
        description.setText(userInput);
        this.container.addView(cardView);

        TextView cancel = cardView.findViewById(R.id.textViewCancel);
        cancel.setVisibility(View.VISIBLE);
        if(meet.equals("We met")){
            cancel.setVisibility(View.GONE);
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirmation");
                builder.setMessage("Are you sure you want to cancel your visit?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        db.collection("visits").document(visitDocumentId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getActivity(), "Visit successfully canceled", Toast.LENGTH_SHORT).show();
                                    Log.d("UpdateStatus", "DocumentSnapshot successfully deleted!");
                                })
                                .addOnFailureListener(e -> Log.w("UpdateStatus", "Error updating document", e));
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked the No button, do nothing
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

}