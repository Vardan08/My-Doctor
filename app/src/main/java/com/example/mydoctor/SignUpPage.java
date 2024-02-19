package com.example.mydoctor;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;

public class SignUpPage extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView targetImageView; // Added variable to track the target ImageView
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText mobileNumberEditText;
    private EditText locationEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    Button signUpButton, attachPhotoBtn, extraButton1, extraButton2;
    Spinner spinner;
    ArrayList<String> spinnerArrList;
    ArrayAdapter<String> spinnerArrAdapter;
    ImageView smallPhotoImageView, extraImageView1, extraImageView2;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        // Initialize views
        TextView alreadyUserTextView = findViewById(R.id.already_user);
        signUpButton = findViewById(R.id.signUpBtn);
        spinner = findViewById(R.id.spinner);
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
        locationEditText = findViewById(R.id.location);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirmPassword);

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
        spinner.setAdapter(spinnerArrAdapter);

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

                // Check if any of the fields is empty
                if (fullName.isEmpty() || email.isEmpty() || mobileNumber.isEmpty() ||
                        location.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
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

                String selectedRole = spinner.getSelectedItem().toString();
                if ("Doctor".equals(selectedRole) && smallPhotoImageView.getDrawable() != null
                        && extraImageView1.getDrawable() != null && extraImageView2.getDrawable() != null) {

                }


                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(SignUpPage.this,task -> {
                        if (task.isSuccessful()) {
                            // Registration success
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Creating a user object to store in Firestore
                                User newUser = new User(fullName,email,mobileNumber,location,password,selectedRole);
                                // Add additional user details to Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users").document(firebaseUser.getUid())
                                        .set(newUser)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Document successfully added!");

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

                                            // Navigate to login page or show success message
                                            openLoginPage();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error adding document", e);
                                            // Handle the error
                                        });
                            }
                        }else {
                            // If sign up fails, display a message to the user.
                            if (task.getException() != null) {
                                Toast.makeText(SignUpPage.this, "Authentication failed: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignUpPage.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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
                    imageUri = null;
                    setButtonVisibility(View.GONE, attachPhotoBtn, extraButton1, extraButton2);
                    setImageViewVisibility(View.GONE, smallPhotoImageView, extraImageView1, extraImageView2);
                    // Increase the top margin for the "Sign Up" button when buttons are gone
                    setSignUpButtonTopMargin(0);
                } else {
                    setButtonVisibility(View.VISIBLE, attachPhotoBtn, extraButton1, extraButton2);
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
