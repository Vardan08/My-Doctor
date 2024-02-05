package com.example.mydoctor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Calendar;

public class ChildrenPatientRegistration extends Fragment {

    private LinearLayout container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_children_patient_registration, container, false);
        this.container = view.findViewById(R.id.container);
        Button addChildButton = view.findViewById(R.id.addChild);
        addChildButton.setOnClickListener(this::showAddChildDialog);
        return view;
    }

    public void showAddChildDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_add_child, null);
        builder.setView(dialogView);

        EditText editTextFullName = dialogView.findViewById(R.id.editTextFullName);
        EditText editTextLocation = dialogView.findViewById(R.id.editTextLocation);
        EditText editTextDOB = dialogView.findViewById(R.id.editTextDOB);
        EditText editTextBirthCertDetails = dialogView.findViewById(R.id.editTextBirthCertDetails);

        editTextDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    (view1, year, month, dayOfMonth) -> {
                        String formattedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        editTextDOB.setText(formattedDate);
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
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
                    addChildCard(fullName);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void addChildCard(String fullName) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);

        TextView fullNameTextView = cardView.findViewById(R.id.textView3);
        fullNameTextView.setText(fullName);

        this.container.addView(cardView);

        cardView.setOnClickListener(v -> {
            PatientQuestionnaire anotherFragment = new PatientQuestionnaire();
            anotherFragment.setChildName(fullName);

            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commit();
        });
    }
}
