package com.example.personalhealthcard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.Calendar;

public class ChildrenPatientRegistration extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_children_patient_registration, container, false);
        Button addChildButton = view.findViewById(R.id.addChild);
        addChildButton.setOnClickListener(this::showAddChildDialog);
        return view;
    }
    public void showAddChildDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_child, null);
        builder.setView(dialogView);

        EditText editTextFullName = dialogView.findViewById(R.id.editTextFullName);
        EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);
        EditText editTextDOB = dialogView.findViewById(R.id.editTextDOB);
        ImageView imageViewBirthCertificate = dialogView.findViewById(R.id.imageViewBirthCertificate);

        // Date Picker for Date of Birth
        editTextDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view1, year1, monthOfYear, dayOfMonth) -> editTextDOB.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1), year, month, day);
            datePickerDialog.show();
        });

        // Image Upload for Birth Certificate
        imageViewBirthCertificate.setOnClickListener(v -> {
            // Implement your code to upload the image
        });

        builder.setPositiveButton("Add", (dialog, id) -> {
            // Handle the data (e.g., save it)
            String fullName = editTextFullName.getText().toString();
            String location = editTextLocation.getText().toString();
            String dob = editTextDOB.getText().toString();
            // Save or process the data
        });

        builder.setNegativeButton("Cancel", (dialog, id) -> {
            // User cancelled the dialog
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

}