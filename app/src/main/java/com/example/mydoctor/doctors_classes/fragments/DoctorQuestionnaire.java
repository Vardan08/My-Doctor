package com.example.mydoctor.doctors_classes.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.example.mydoctor.R;

import java.util.ArrayList;


public class DoctorQuestionnaire extends Fragment {
    Spinner receiving;
    ArrayList receivingArrayList;
    ArrayAdapter receivingAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_doctors_questionnaire, container, false);
        receiving = view.findViewById(R.id.receiving);
        receivingArrayList = new ArrayList();
        receivingArrayList.add("is received?");
        receivingArrayList.add("received");
        receivingArrayList.add("don't received");
        receivingAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, receivingArrayList);
        receivingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        receiving.setAdapter((SpinnerAdapter) receivingAdapter);
        return view;
    }
}