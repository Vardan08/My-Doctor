package com.example.mydoctor.patients_classes.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mydoctor.R;
import com.example.mydoctor.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildrenPatientRegistration extends Fragment {

    private LinearLayout container;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Map<String, Object> child = new HashMap<>();
    String doctorName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("USER");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_children_patient_registration, container, false);
        this.container = view.findViewById(R.id.container);
        Button addChildButton = view.findViewById(R.id.addChild);
        addChildButton.setOnClickListener(this::showAddChildDialog);

        if (currentUser != null) {
            fetchChildrenAndDisplay();
        } else {
            Toast.makeText(getActivity(), "User not signed in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchChildrenAndDisplay() {
        if (currentUser == null) return;

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Loading children...");
        progressDialog.show();

        db.collection("users").document(currentUser.getUid()).collection("children")
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();

                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String fullName = document.getString("fullName");
                            doctorName = document.getString("Doctor");
                            if (fullName != null && doctorName != null) {
                                fetchDoctorAndAddChildCard(fullName,doctorName);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchDoctorAndAddChildCard(String childFullName, String doctorId) {
        db.collection("users").document(doctorId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String doctorFullName = documentSnapshot.getString("fullName"); // Assuming the doctor's name is stored under "fullName"
                String displayText = childFullName + " - Doctor: " + doctorFullName;
                addChildCard(childFullName, doctorFullName); // Updated method to handle both names
            }
        }).addOnFailureListener(e -> {
            // Handle any errors, such as the doctor's document not existing
        });
    }
    public void showAddChildDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_child, null);
        builder.setView(dialogView);

        EditText editTextFullName = dialogView.findViewById(R.id.editTextFullName);
        EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);
        EditText editTextDOB = dialogView.findViewById(R.id.editTextDOB);
        EditText editTextBirthCertDetails = dialogView.findViewById(R.id.editTextBirthCertDetails);

        // Initialize spinners
        Spinner regions = dialogView.findViewById(R.id.regions);
        Spinner cities = dialogView.findViewById(R.id.cities);
        Spinner clinics = dialogView.findViewById(R.id.clinics);
        Spinner doctors = dialogView.findViewById(R.id.doctors);
        fetchRegionsAndPopulateSpinner(regions,cities,clinics,doctors);


        editTextDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view1, year, month, dayOfMonth) -> editTextDOB.setText(String.format("%d/%d/%d", dayOfMonth, month + 1, year)),
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", (dialog, id) -> {});

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            addButton.setOnClickListener(view1 -> {
                String fullName = editTextFullName.getText().toString().trim();
                String location = editTextLocation.getText().toString().trim();
                String dob = editTextDOB.getText().toString().trim();
                String birthCertDetails = editTextBirthCertDetails.getText().toString().trim();


                if (fullName.isEmpty() || location.isEmpty() || dob.isEmpty() || birthCertDetails.isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Add spinner values as needed
                    addChildToFirestore(fullName, location, dob, birthCertDetails);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void setupSpinner(Spinner spinner, int arrayResourceId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                arrayResourceId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }



    private void addChildToFirestore(String fullName, String location, String dob, String birthCertDetails) {
        if (currentUser == null) return;

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Adding child...");
        progressDialog.show();


        child.put("fullName", fullName);
        child.put("location", location);
        child.put("dob", dob);
        child.put("birthCertDetails", birthCertDetails);

        db.collection("users").document(currentUser.getUid()).collection("children").add(child)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getActivity(), "Child added successfully!", Toast.LENGTH_SHORT).show();
                    addChildCard(fullName,doctorName);
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error adding child", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                });
    }
    private void fetchRegionsAndPopulateSpinner(Spinner regionsSpinner, Spinner citiesSpinner, Spinner clinicsSpinner,Spinner doctorsSpinner) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading regions...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<String> regionsList = new ArrayList<>();
        Map<String, String> regionIdMap = new HashMap<>(); // To map region names to their IDs
        regionsList.add("Choose region");

        db.collection("regions")
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String regionName = document.getString("name");
                            String regionId = document.getId(); // Assuming the document ID is the region ID
                            if (regionName != null) {
                                regionsList.add(regionName);
                                regionIdMap.put(regionName, regionId);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, regionsList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        regionsSpinner.setAdapter(adapter);

                        regionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedRegion = (String) parent.getItemAtPosition(position);
                                if (regionIdMap.containsKey(selectedRegion)) {
                                    String selectedRegionId = regionIdMap.get(selectedRegion);
                                    fetchCitiesAndPopulateSpinner(selectedRegionId, citiesSpinner,clinicsSpinner,doctorsSpinner);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                citiesSpinner.setVisibility(View.GONE);
                            }
                        });

                        // Make the cities spinner invisible initially
                        citiesSpinner.setVisibility(View.GONE);
                        clinicsSpinner.setVisibility(View.GONE);
                        doctorsSpinner.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getActivity(), "Error loading regions", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void fetchCitiesAndPopulateSpinner(String regionId, Spinner citiesSpinner, Spinner clinicsSpinner,Spinner doctorsSpinner) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading cities...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<String> citiesList = new ArrayList<>();
        Map<String, String> cityIdMap = new HashMap<>();
        citiesList.add("Choose city");

        db.collection("cities")
                .whereEqualTo("regionId", regionId)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String cityName = document.getString("name");
                            String cityId = document.getId(); // Assuming the document ID is the city ID
                            if (cityName != null) {
                                citiesList.add(cityName);
                                cityIdMap.put(cityName, cityId);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, citiesList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        citiesSpinner.setAdapter(adapter);

                        citiesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedCity = (String) parent.getItemAtPosition(position);
                                if(cityIdMap.containsKey("Choose city")){
                                    clinicsSpinner.setVisibility(View.GONE);
                                    doctorsSpinner.setVisibility(View.GONE);
                                } else if (cityIdMap.containsKey(selectedCity)) {
                                    String selectedCityId = cityIdMap.get(selectedCity);
                                    fetchClinicsAndPopulateSpinner(selectedCityId, clinicsSpinner,doctorsSpinner);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                clinicsSpinner.setVisibility(View.GONE);
                            }
                        });


                        citiesSpinner.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getActivity(), "Error loading cities", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void fetchClinicsAndPopulateSpinner(String cityId, Spinner clinicsSpinner, Spinner doctorsSpinner) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading clinics...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<String> clinicsList = new ArrayList<>();
        Map<String, String> clinicIdMap = new HashMap<>();
        clinicsList.add("Choose clinic");

        db.collection("clinics")
                .whereEqualTo("cityId", cityId)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String clinicName = document.getString("name");
                            String clinicId = document.getId();
                            if (clinicName != null) {
                                clinicsList.add(clinicName);
                                clinicIdMap.put(clinicName, clinicId);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, clinicsList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        clinicsSpinner.setAdapter(adapter);

                        clinicsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if (position > 0) { // Assuming the first item is "Choose clinic" and should be ignored
                                    String selectedClinic = (String) parent.getItemAtPosition(position);
                                    String selectedClinicId = clinicIdMap.get(selectedClinic);
                                    if (selectedClinicId != null) {
                                        fetchDoctorsAndPopulateSpinner(selectedClinicId, doctorsSpinner);
                                    }else {
                                        Toast.makeText(getActivity(), "kjdfvh;fkjdvklfdvjk", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                doctorsSpinner.setVisibility(View.GONE);
                            }
                        });

                        clinicsSpinner.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getActivity(), "Error loading clinics", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void fetchDoctorsAndPopulateSpinner(String clinicId, Spinner doctorsSpinner) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Loading doctors...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        List<String> doctorsList = new ArrayList<>();
        Map<String, String> doctorIdMap = new HashMap<>(); // If you need to map doctor names to their IDs
        doctorsList.add("Choose doctor");

        db.collection("users")
                .whereEqualTo("roll", "Doctor")
                .whereEqualTo("clinicId", clinicId)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String doctorName = document.getString("fullName"); // Assuming there's a 'name' field
                            String doctorId = document.getId();
                            if (doctorName != null) {
                                doctorsList.add(doctorName);
                                doctorIdMap.put(doctorName, doctorId);
                            }else {
                                Toast.makeText(getActivity(), "djfldshklhj", Toast.LENGTH_SHORT).show();
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, doctorsList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        doctorsSpinner.setAdapter(adapter);

                        doctorsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if(position > 0){
                                    String selectedDoctor = (String) parent.getItemAtPosition(position);
                                    String selectedDoctorId = doctorIdMap.get(selectedDoctor);
                                    if(selectedDoctorId != null) {
                                        child.put("Doctor",selectedDoctorId);
                                    }
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });

                        doctorsSpinner.setVisibility(View.VISIBLE); // Make the doctors spinner visible upon successful loading
                    } else {
                        Toast.makeText(getActivity(), "Error loading doctors", Toast.LENGTH_SHORT).show();
                    }
                });

    }





    private void addChildCard(String fullName, String doctorFullName) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);

        TextView fullNameTextView = cardView.findViewById(R.id.textView3);
        fullNameTextView.setText(fullName);

        // Assuming you have another TextView with id textViewDoctor for the doctor's name
        TextView doctorNameTextView = cardView.findViewById(R.id.textViewDoctor);
        doctorNameTextView.setText("Doctor: " + doctorFullName);

        this.container.addView(cardView);

        cardView.setOnClickListener(v -> {
            PatientQuestionnaire anotherFragment = new PatientQuestionnaire();
            anotherFragment.setChildName(fullName); // Consider passing doctorFullName too if needed in the fragment

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();
        });
    }

}
