package com.example.personalhealthcard;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;

public class TodayPatients extends Fragment {
    Spinner spinner;
    ArrayList spinnerArrList;
    ArrayAdapter spinnerArrAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_patients, container, false);
        ConstraintLayout patient = view.findViewById(R.id.patient);
            spinner = view.findViewById(R.id.spinner2);
            spinnerArrList = new ArrayList();
            spinnerArrList.add("Today Patients");
            spinnerArrList.add("Unvaccinated Patients");
            spinnerArrAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, spinnerArrList);
            spinnerArrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            spinner.setAdapter(spinnerArrAdapter);

        patient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTodayPatientsPage();
            }
        });

        return view;
    }

    private void openTodayPatientsPage() {
        DoctorQuestionnaire anotherFragment = new DoctorQuestionnaire();

        androidx.fragment.app.FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);

        fragmentTransaction.addToBackStack(null);

        fragmentTransaction.commit();
    }

}