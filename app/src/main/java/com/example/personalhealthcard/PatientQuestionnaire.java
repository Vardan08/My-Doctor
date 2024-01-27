package com.example.personalhealthcard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class PatientQuestionnaire extends Fragment {

    private String childName;

    public PatientQuestionnaire() {
        // Required empty public constructor
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patient_questionnaire, container, false);

        TextView titleTextView = view.findViewById(R.id.titleTextView);
        if (childName != null && !childName.isEmpty()) {
            titleTextView.setText(childName + " Questionnaire");
        } else {
            titleTextView.setText("Patient Questionnaire");
        }

        // Additional setup code (if any)

        return view;
    }
}
