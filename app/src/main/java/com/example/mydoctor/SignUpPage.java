package com.example.mydoctor;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mydoctor.data_structures.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpPage extends AppCompatActivity {
    public static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView targetImageView; // Added variable to track the target ImageView
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText mobileNumberEditText;
    private EditText locationEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private List<String> regionIds = new ArrayList<>();
    private String selectedClinicId;
    List<String> clinicIds = new ArrayList<>();

    ProgressDialog dialog;
    Button signUpButton, attachPhotoBtn, extraButton1, extraButton2, addTimeButton;
    Spinner spinner, regions, cities, clinics;
    ArrayList<String> spinnerArrList;
    ArrayAdapter<String> spinnerArrAdapter;
    ImageView smallPhotoImageView, extraImageView1, extraImageView2;
    List<String> regionsList, citiesList;
    FirebaseFirestore db;
    List<String> cityIds;
    List<String> cityNames;
    List<String> clinicsList = new ArrayList<>();
    ArrayAdapter<String> clinicsAdapter;
    private LinearLayout timesLayout;
    private ArrayList<String> timeSet = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        // Initialize views
        TextView alreadyUserTextView = findViewById(R.id.already_user);
        timesLayout = findViewById(R.id.times_container);
        signUpButton = findViewById(R.id.signUpBtn);
        spinner = findViewById(R.id.spinner);
        regions = findViewById(R.id.regions);
        cities = findViewById(R.id.cities);
        clinics = findViewById(R.id.clinics);
        attachPhotoBtn = findViewById(R.id.attachPhotoBtn);
        smallPhotoImageView = findViewById(R.id.smallPhotoImageView);
        extraButton1 = findViewById(R.id.attachPhotoBtn1);
        extraButton2 = findViewById(R.id.attachPhotoBtn2);
        extraImageView1 = findViewById(R.id.smallPhotoImageView1);
        extraImageView2 = findViewById(R.id.smallPhotoImageView2);
        TextView alreadyUser = findViewById(R.id.already_user);
        fullNameEditText = findViewById(R.id.fullName);
        emailEditText = findViewById(R.id.userEmailId);
        mobileNumberEditText = findViewById(R.id.mobileNumber);
        addTimeButton = findViewById(R.id.buttonAddTimes);
        locationEditText = findViewById(R.id.location);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);
        dialog = new ProgressDialog(SignUpPage.this);
        dialog.setCancelable(false);
        dialog.setMessage("Loading.....");
        db = FirebaseFirestore.getInstance();

        if (savedInstanceState != null) {
            fullNameEditText.setText(savedInstanceState.getString("fullName"));
            emailEditText.setText(savedInstanceState.getString("email"));
            mobileNumberEditText.setText(savedInstanceState.getString("mobileNumber"));
            locationEditText.setText(savedInstanceState.getString("location"));
            passwordEditText.setText(savedInstanceState.getString("password"));
            confirmPasswordEditText.setText(savedInstanceState.getString("confirmPassword"));
        }

        // Create a SpannableString with the desired text
        SpannableString spannableString = new SpannableString("Already a user? Login here");

        // Create a ClickableSpan for the clickable text
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // Handle the click event, for example, navigate to another page
                openLoginPage();
            }
        };

        // Set the ClickableSpan on the desired portion of the text
        spannableString.setSpan(clickableSpan, spannableString.length() - "Login here".length(), spannableString.length(), 0);

        // Apply the SpannableString to the TextView
        alreadyUserTextView.setText(spannableString);

        // Enable the movement method for the TextView to make the links clickable
        alreadyUserTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Set up Spinner
        spinnerArrList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.user_roles)));
        spinnerArrAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerArrList);
        spinnerArrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter((SpinnerAdapter) spinnerArrAdapter);

        regionsList = new ArrayList<>();
        regionsList.add("Choose Region");
        ArrayAdapter<String> regionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regionsList);
        regionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regions.setAdapter(regionAdapter);
        db.collection("regions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                regionIds.clear();
                regionsList.clear(); // Clear existing items
                regionsList.add("Choose Region"); // Add initial prompt
                regionIds.add(null); // Corresponding entry for the initial prompt

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String docName = document.getString("name");
                    String docId = document.getId(); // Assuming you want to track region IDs

                    regionsList.add(docName);
                    regionIds.add(docId); // Make sure this matches the `regionsList`
                }
                // Notify the adapter about data changes
                regionAdapter.notifyDataSetChanged();
            } else {
                Log.d("FirestoreError", "Error getting documents: ", task.getException());
            }
        });

        regions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position < regionIds.size()) { // Check bounds
                    String selectedRegionId = regionIds.get(position);
                    loadCitiesForRegion(selectedRegionId);
                    cities.setVisibility(View.VISIBLE);
                } else {
                    cities.setVisibility(View.GONE);
                    clinics.setVisibility((View.GONE));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Nothing to do here
            }
        });


        // Set up Attach Photo button click listener
        attachPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the target ImageView for the main photo
                targetImageView = smallPhotoImageView;
                openImageChooser(targetImageView);
            }
        });

        // Set up Sign Up button click listener
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input from EditText fields
                String fullName = fullNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String mobileNumber = mobileNumberEditText.getText().toString().trim();
                String location = locationEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();
                String clinicsStr = clinics.toString().trim();
                String citiesStr = cities.toString().trim();
                String regionsStr = regions.toString().trim();
                Toast.makeText(SignUpPage.this, ""+timeSet, Toast.LENGTH_SHORT).show();

                String selectedRole = spinner.getSelectedItem().toString();
                if (!"Patient".equals(selectedRole) && (regions.getSelectedItemPosition() <= 0 ||
                        cities.getSelectedItemPosition() <= 0 || clinics.getSelectedItemPosition() <= 0)) {
                    Toast.makeText(SignUpPage.this, "Please select region, city, and clinic", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if any region, city, or clinic is not selected
                }

                if (!"Patient".equals(selectedRole)&&(smallPhotoImageView.getDrawable() == null || extraImageView1.getDrawable() == null || extraImageView2.getDrawable() == null)) {
                    Toast.makeText(SignUpPage.this, "Please attach a photo", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if photo is not attached
                }
                if (!"Patient".equals(selectedRole) && timeSet == null) {
                    Toast.makeText(SignUpPage.this, "Please choose a time", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if photo is not attached
                }

                // Check if any of the fields is empty
                if (fullName.isEmpty() || email.isEmpty() || mobileNumber.isEmpty() ||
                        location.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                        clinicsStr.isEmpty() || clinicsStr.equals("Choose Clinic") || citiesStr.isEmpty() ||
                        citiesStr.equals("Choose City") || regionsStr.isEmpty() || regionsStr.equals("Choose Region")) {
                    Toast.makeText(SignUpPage.this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if any field is empty
                }

                // Validate email format using a regular expression
                if (!isValidEmail(email)) {
                    Toast.makeText(SignUpPage.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if email is not valid
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignUpPage.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution if passwords do not match
                }


                // Additional validation checks if needed
                dialog.show();

                // Check if the phone number is already in use
                db.collection("users").whereEqualTo("mobileNumber", mobileNumber).get().addOnCompleteListener(task -> {

                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Phone number is already in use
                        dialog.dismiss();
                        Toast.makeText(SignUpPage.this, "This phone number is already in use.", Toast.LENGTH_SHORT).show();
                    } else if (task.isSuccessful()) {
                        // Phone number is not in use, proceed with creating the user
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignUpPage.this, createUserTask -> {
                            if (createUserTask.isSuccessful()) {
                                // Registration success
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Send verification email
                                    firebaseUser.sendEmailVerification().addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            Log.d(TAG, "Email verification sent.");
                                            // Notify the user to check their email for verification
                                            Toast.makeText(SignUpPage.this, "Registration successful. Please check your email for verification.", Toast.LENGTH_LONG).show();
                                        } else {
                                            Log.e(TAG, "sendEmailVerification", task2.getException());
                                            Toast.makeText(SignUpPage.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    // Assuming you create a User object to store in Firestore
                                    User newUser;
                                    if ("Doctor".equals(selectedRole)) {
                                        newUser = new User(fullName, email, mobileNumber, location, password, selectedRole, selectedClinicId,timeSet);
                                        // The rest of your registration process follows
                                    } else {
                                        // For other roles where clinicId is not required
                                        newUser = new User(fullName, email, mobileNumber, location, password, selectedRole, null,null);
                                        // The rest of your registration process follows
                                    }

                                    // Add additional user details to Firestore
                                    db.collection("users").document(firebaseUser.getUid())
                                            .set(newUser)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "Document successfully added!");
                                                if ("Doctor".equals(selectedRole) && smallPhotoImageView.getDrawable() != null
                                                        && extraImageView1.getDrawable() != null && extraImageView2.getDrawable() != null) {
                                                    uploadImages(firebaseUser.getUid(),smallPhotoImageView,extraImageView1,extraImageView2);
                                                }
                                                // Clear all EditText fields here
                                                fullNameEditText.setText("");
                                                emailEditText.setText("");
                                                mobileNumberEditText.setText("");
                                                locationEditText.setText("");
                                                passwordEditText.setText("");
                                                confirmPasswordEditText.setText("");

                                                // Optionally, reset the spinner
                                                spinner.setSelection(0); // Assuming the first position is the default

                                                // Clear images or reset to default if applicable
                                                clearImages(smallPhotoImageView, extraImageView1, extraImageView2);

                                                timesLayout.removeAllViews();
                                                dialog.dismiss();

                                                // Navigate to login page or show success message
                                                openLoginPage();

                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error adding document", e);
                                                // Handle the error
                                            });
                                }
                            } else {
                                // If sign up fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", createUserTask.getException());
                                if (createUserTask.getException() instanceof FirebaseAuthUserCollisionException) {
                                    Toast.makeText(SignUpPage.this, "This email is already in use.", Toast.LENGTH_SHORT).show();
                                } else if (createUserTask.getException() != null) {
                                    Toast.makeText(SignUpPage.this, "Authentication failed: " + createUserTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(SignUpPage.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }
                        });
                    } else {
                        // Handle error checking phone number
                        Toast.makeText(SignUpPage.this, "Failed to check if phone number is in use.", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });
        addTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTimePickerDialog();
            }
        });

        // Set up Additional Buttons
        extraButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the target ImageView for extra photo 1
                targetImageView = extraImageView1;
                openImageChooser(targetImageView);
            }
        });

        extraButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set the target ImageView for extra photo 2
                targetImageView = extraImageView2;
                openImageChooser(targetImageView);
            }
        });

        // Set up Spinner item selected listener to hide/show buttons
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedRole = spinner.getSelectedItem().toString();
                if ("Patient".equals(selectedRole)) {
                    regions.setVisibility(View.GONE);
                    cities.setVisibility(View.GONE);
                    clinics.setVisibility(View.GONE);
                    imageUri = null;
                    setButtonVisibility(View.GONE, attachPhotoBtn, extraButton1, extraButton2,addTimeButton);
                    setImageViewVisibility(View.GONE, smallPhotoImageView, extraImageView1, extraImageView2);
                    // Increase the top margin for the "Sign Up" button when buttons are gone
                    setSignUpButtonTopMargin(0);
                } else {
                    regions.setVisibility(View.VISIBLE);
                    cities.setVisibility(View.VISIBLE);
                    clinics.setVisibility(View.VISIBLE);
                    setButtonVisibility(View.VISIBLE, attachPhotoBtn, extraButton1, extraButton2,addTimeButton);
                    setImageViewVisibility(View.VISIBLE, smallPhotoImageView, extraImageView1, extraImageView2);
                    // Reset the top margin for the "Sign Up" button
                    setSignUpButtonTopMargin(32);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }
    private void openTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = String.format("%02d:%02d", hourOfDay, minute);
                if (timeSet.contains(time)) {
                    // Time already exists, show a toast message
                    Toast.makeText(SignUpPage.this, "This hour has already been chosen.", Toast.LENGTH_SHORT).show();
                } else {
                    // Add the new time to the set and UI
                    timeSet.add(time);
                    addTimeEntry(time);
                }
            }
        }, 12, 0, true);
        timePickerDialog.show();
    }
    private void addTimeEntry(String time) {
        // Create a new horizontal LinearLayout for each time entry
        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        entryLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create a TextView for the time
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        textView.setText(time);

        // Create a delete button with an X icon
        ImageView deleteButton = new ImageView(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        deleteButton.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_delete));
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Remove the time entry from the layout and the set
                timesLayout.removeView(entryLayout);
                timeSet.remove(time);
            }
        });

        // Add the TextView and the delete button to the horizontal LinearLayout
        entryLayout.addView(textView);
        entryLayout.addView(deleteButton);

        // Add the entry layout to the main times layout
        timesLayout.addView(entryLayout);
    }
    private void loadCitiesForRegion(String regionId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("cities")
                .whereEqualTo("regionId", regionId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        cityNames = new ArrayList<>();
                        cityNames.add("Choose City");
                        cityIds = new ArrayList<>(); // Если вам нужны ID городов

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            cityNames.add(document.getString("name")); // Предполагается, что у документов есть поле name
                            cityIds.add(document.getId()); // Получаем ID документа
                        }

                        // Теперь у вас есть список городов, вы можете обновить ваш spinner2
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SignUpPage.this, android.R.layout.simple_spinner_dropdown_item, cityNames);
                        cities.setAdapter(adapter);
                        cities.setVisibility(View.VISIBLE); // Показываем spinner2 с загруженными городами
                    } else {
                        Log.w("Firestore", "Error getting documents.", task.getException());
                    }
                });
        cities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    // Code to load clinics based on the selected city
                    String selectedCityId = cityIds.get(position - 1); // Assuming you have a similar list for city IDs
                    loadClinicsForCity(selectedCityId);
                    clinics.setVisibility(View.VISIBLE);
                } else {
                    clinics.setVisibility(View.GONE); // Hide or reset clinics if "Choose City" is selected
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });




    }
    private void loadClinicsForCity(String cityId) {
        clinics.setAdapter(clinicsAdapter);
        clinicsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clinicsList);
        clinicsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clinics.setAdapter(clinicsAdapter);
        // Clear the existing clinics list (except for the initial "Choose Clinic" placeholder)
        if (clinicsList.size() > 1) {
            clinicsList.subList(1, clinicsList.size()).clear();
        }
        // Query Firestore for clinics in the selected city
        db.collection("clinics")
                .whereEqualTo("cityId", cityId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            clinicsList.add("Choose Clinic");
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String clinicName = document.getString("name"); // Assuming each clinic document has a 'name' field
                                clinicsList.add(clinicName);
                                String clinicIdsStr = document.getId();
                                Log.d("ids",clinicIdsStr);
                                clinicIds.add(clinicIdsStr);
                            }
                            // Notify the clinicsAdapter that its dataset has changed to refresh the spinner
                            clinicsAdapter.notifyDataSetChanged();
                        } else {
                            Log.w(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        clinics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    // Assuming position 0 is "Choose Clinic", and clinicIds is aligned with your clinicsList
                    selectedClinicId = clinicIds.get(position - 1);
                } else {
                    selectedClinicId = null; // No clinic or "Choose Clinic" selected
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private void uploadImages(String userId, ImageView... images) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        List<Task<Uri>> uploadTasks = new ArrayList<>();

        for (int i = 0; i < images.length; i++) {
            ImageView imageView = images[i];
            String imageName = "image" + i + ".jpg"; // Or generate a unique name based on the user ID
            StorageReference imageRef = storageRef.child("images/" + userId + "/" + imageName);
            imageView.setDrawingCacheEnabled(true);
            imageView.buildDrawingCache();
            Bitmap bitmap = imageView.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTasks.add(uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return imageRef.getDownloadUrl();
            }));
        }

        Tasks.whenAllSuccess(uploadTasks).addOnSuccessListener(results -> {
            // Here you have all the image URLs in results, you can store them in Firestore
            Map<String, Object> imageUrls = new HashMap<>();
            for (int i = 0; i < results.size(); i++) {
                Uri downloadUri = (Uri) results.get(i);
                imageUrls.put("imageUrl" + i, downloadUri.toString());
            }

            // Update the user's document with the image URLs
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update(imageUrls)
                    .addOnSuccessListener(aVoid -> {
                        // Images uploaded and URLs saved to Firestore successfully
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        // Handle the error
                        dialog.dismiss();
                    });
        }).addOnFailureListener(e -> {
            // Handle failure in uploading images
            dialog.dismiss();
        });
    }

    private void clearImages(ImageView... images) {
        for (ImageView image : images) {
            image.setImageDrawable(null); // Removes the image
            // Use setImageResource(R.drawable.default_image) if you want to reset to a default image
        }
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }

    private void openLoginPage() {
        // Handle the navigation to the login page here
        Intent intent = new Intent(this, LoginPage.class);
        finish();
        startActivity(intent);
    }

    private void openImageChooser(ImageView targetImageView) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

        // Set the target ImageView for displaying the selected image
        if (targetImageView != null) {
            targetImageView.setVisibility(View.VISIBLE);
        }
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the state of EditText fields
        outState.putString("fullName", fullNameEditText.getText().toString());
        outState.putString("email", emailEditText.getText().toString());
        outState.putString("mobileNumber", mobileNumberEditText.getText().toString());
        outState.putString("location", locationEditText.getText().toString());
        outState.putString("password", passwordEditText.getText().toString());
        outState.putString("confirmPassword", confirmPasswordEditText.getText().toString());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Toast.makeText(this, "Photo selected", Toast.LENGTH_SHORT).show();

            // Determine which ImageView should be updated based on the clicked button
            if (targetImageView != null) {
                // Set the selected image to the corresponding ImageView
                targetImageView.setImageURI(imageUri);
            }
        }
    }

    private void setImageViewVisibility(int visibility, ImageView... imageViews) {
        for (ImageView imageView : imageViews) {
            imageView.setVisibility(visibility);
            imageView.setImageURI(null);
        }
    }


    // Method to set the visibility of multiple buttons
    private void setButtonVisibility(int visibility, View... buttons) {
        for (View button : buttons) {
            button.setVisibility(visibility);
        }
    }

    // Method to set the top margin for the "Sign Up" button
    private void setSignUpButtonTopMargin(int margin) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) signUpButton.getLayoutParams();
        params.topMargin = margin;
        signUpButton.setLayoutParams(params);
    }
}
