package com.example.mydoctor.patients_classes.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mydoctor.LoginPage;
import com.example.mydoctor.R;
import com.example.mydoctor.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PatientsProfile extends Fragment {
    private User user;
    private ImageView imageView;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    TextView fullName, email, phoneNumber, nameBelowPhoto, deleteAccountTextView;
    Button logOut;
    ProgressDialog dialog;
    // Firebase Storage
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable("USER");
        }
        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patients_profile, container, false);

        fullName = view.findViewById(R.id.textName);
        email = view.findViewById(R.id.textEmail);
        phoneNumber = view.findViewById(R.id.textPhone);
        logOut = view.findViewById(R.id.logOut);
        nameBelowPhoto = view.findViewById(R.id.textView3);
        deleteAccountTextView = view.findViewById(R.id.deleteAccount);
        imageView = view.findViewById(R.id.profilePhoto);

        if (user != null) {
            nameBelowPhoto.setText(user.getFullName());
            fullName.setText(user.getFullName());
            email.setText(user.getEmail());
            phoneNumber.setText(user.getMobileNumber());
        } else {
            Log.d("ProfileFragment", "User data is not available");
        }
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("profileImageUrl")) {
                            String imageUrl = documentSnapshot.getString("profileImageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                // Использование Glide для загрузки изображения
                                Glide.with(this)
                                        .load(imageUrl)
                                        .into(imageView);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.d("Firestore", "Error getting document: ", e));
        }

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            imageView.setImageURI(imageUri);
                            uploadImageToFirebase(imageUri);
                        }
                    }
                });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intent);
            }
        });

        deleteAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        return view;
    }

    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Показываем ProgressDialog при начале загрузки
            progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Reference to store file at "images/{userId}/{timestamp}.jpg"
            StorageReference fileRef = storageRef.child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + System.currentTimeMillis() + ".jpg");
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Скрываем ProgressDialog при успешной загрузке
                            progressDialog.dismiss();

                            // Get the URL of the uploaded file
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageUrl = uri.toString();
                                    // Update user document with imageUrl
                                    updateUserProfileImage(imageUrl);
                                }
                            });
                            Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Скрываем ProgressDialog при ошибке загрузки
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void updateUserProfileImage(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            progressDialog.setTitle("Updating Profile...");
            progressDialog.show(); // Показать диалог перед обновлением

            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .update("profileImageUrl", imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss(); // Закрыть диалог после успешного обновления
                        Log.d("Firestore", "DocumentSnapshot successfully updated!");
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss(); // Закрыть диалог в случае ошибки
                        Log.w("Firestore", "Error updating document", e);
                    });
        }
    }


    private void deleteAccount() {
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);
        dialog.setMessage("Loading.....");
        dialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String uid = user.getUid();
            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "User document deleted successfully.");
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d("Delete Account", "User account deleted.");
                                FirebaseAuth.getInstance().signOut();
                                if(dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                openLoginActivity();
                            } else {
                                Log.w("Delete Account", "Failed to delete user account.", task.getException());
                                if(dialog != null && dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.w("Firestore", "Error deleting user document", e);
                        if(dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    });
        }else{
            Log.d("Delete Account", "No user to delete.");
            if(dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }


    private void openLoginActivity() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(getActivity(), LoginPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Очистите back stack
        startActivity(intent);
    }
}