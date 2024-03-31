package com.example.mydoctor.patients_classes.fragments;

import static com.example.mydoctor.SignUpPage.PICK_IMAGE_REQUEST;
import static com.google.common.io.Files.getFileExtension;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.example.mydoctor.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChildrenPatientRegistration extends Fragment {
    private String currentChildIdForImageUpload;
    private ImageView activeImageView;
    private LinearLayout container;
    private User user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    Map<String, Object> child = new HashMap<>();
    String doctorName;
    ImageView childImageView;
    private Map<String, ImageView> childIdToImageViewMap = new HashMap<>();

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
                            String doctorId = document.getString("Doctor");
                            if (fullName != null && doctorId != null) {
                                fetchDoctorAndAddChildCard(document, doctorId);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchDoctorAndAddChildCard(DocumentSnapshot child, String doctorId) {
        db.collection("users").document(doctorId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                doctorName = documentSnapshot.getString("fullName");
                if (doctorName != null) {
                    addChildCard(child, doctorName);
                }
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
                    Log.d("myDcRef",documentReference+"");
                    Toast.makeText(getActivity(), "Child added successfully!", Toast.LENGTH_SHORT).show();
                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    // Document was found
                                    addChildCard(document,doctorName);
                                    progressDialog.dismiss();
                                } else {
                                    // No such document
                                    Log.d("TAG", "No such document");
                                }
                            }else {
                                // Task failed with an exception
                                Log.d("get failed with ", task.getException() + "");
                            }
                        }
                    });


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
                            doctorName = document.getString("fullName"); // Assuming there's a 'name' field
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
                                    String selectedDoctorName = doctorsList.get(position);
                                    if(selectedDoctorId != null && selectedDoctorName != null) {
                                        child.put("Doctor",selectedDoctorId);
                                        doctorName = selectedDoctorName;
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






    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    private void addChildCard(DocumentSnapshot child, String doctorFullName) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);

        Map<String, Object> user = child.getData();
        assert user != null;
        String fullName = (String) user.get("fullName");
        String childId = child.getId();

        TextView fullNameTextView = cardView.findViewById(R.id.textView3);
        fullNameTextView.setText(fullName);

        TextView doctorNameTextView = cardView.findViewById(R.id.textViewDoctor);
        doctorNameTextView.setText("Dr: " + doctorFullName);

        ImageView childImageView = cardView.findViewById(R.id.imageViewChild);
        // Save ImageView reference with childId
        childIdToImageViewMap.put(childId, childImageView);

        // If imageUri exists, load it
        if (user.containsKey("imageUri")) {
            String imageUri = (String) user.get("imageUri");
            Glide.with(this).load(imageUri).into(childImageView);
        }

        this.container.addView(cardView);

        // Set click listener for the ImageView to select image
        childImageView.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            // Use currentChildIdForImageUpload to identify the child for image upload
            currentChildIdForImageUpload = childId;
        });

        // Additional code for setting other listeners and functionalities
        cardView.setOnClickListener(v -> {
            // Make sure you are not setting the listener again if it's the ImageView that was clicked
            // This is handled by not propagating the click event from the ImageView to the CardView
            PatientQuestionnaire anotherFragment = new PatientQuestionnaire();
            anotherFragment.setChildName(fullName); // Consider passing doctorFullName too if needed in the fragment

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            ImageView childImageView = childIdToImageViewMap.get(currentChildIdForImageUpload);
            if (childImageView != null) {
                childImageView.setImageURI(imageUri); // Display the selected image immediately
                uploadImageToFirebaseStorage(imageUri, currentChildIdForImageUpload); // Upload to Firebase
            }
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, String childId) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("child_images");
        // Use the child ID as part of the file name to ensure uniqueness and easier retrieval
        String fileName = childId + "_" + System.currentTimeMillis() + "." + getFileExtension(getActivity(), imageUri);
        StorageReference fileRef = storageRef.child(fileName);

        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            progressDialog.dismiss();
            Toast.makeText(getActivity(), "Upload successful", Toast.LENGTH_LONG).show();
            fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                // Assuming you have a HashMap that maps child IDs to their ImageView
                ImageView childImageView = childIdToImageViewMap.get(childId);
                if (childImageView != null) {
                    Glide.with(this).load(downloadUri.toString()).into(childImageView);
                }
                saveImageInfoToFirestore(downloadUri.toString(), childId); // Save downloadUri in Firestore
            });
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        });
    }

    /**
     * Get the file extension of the file in the Uri.
     * @param context The context.
     * @param uri The Uri of the file.
     * @return The file extension as a String.
     */
    public static String getFileExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void saveImageInfoToFirestore(String imageUrl, String childId) {
        // Get a reference to the Firestore document of the child
        DocumentReference childDocRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).collection("children").document(childId);

        // Update the document with the new image URL
        childDocRef.update("imageUri", imageUrl)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "DocumentSnapshot successfully updated with new image URI"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error updating document", e));
    }

}
