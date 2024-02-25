package com.example.mydoctor.patients_classes.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.fragment.app.Fragment;

import com.example.mydoctor.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddVisit extends Fragment {
    private Button addEventButton;
    private TextView selectedDateTimeTextView;
    private Calendar selectedDateTime = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_visit, container, false);

        addEventButton = rootView.findViewById(R.id.addEventButton);
        selectedDateTimeTextView = rootView.findViewById(R.id.selectedDateTimeTextView);

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateTimePicker();
            }
        });

        return rootView;
    }

    public void showDateTimePicker() {
        // Get the current date
        int year = selectedDateTime.get(Calendar.YEAR);
        int month = selectedDateTime.get(Calendar.MONTH);
        int day = selectedDateTime.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Update the selected date
                selectedDateTime.set(year, monthOfYear, dayOfMonth);

                // Get the current time
                int hour = selectedDateTime.get(Calendar.HOUR_OF_DAY);
                int minute = selectedDateTime.get(Calendar.MINUTE);

                // Create a TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Update the selected time
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);

                        // Now 'selectedDateTime' contains both the selected date and time
                        updateSelectedDateTimeTextView();
                    }
                }, hour, minute, true); // true for 24-hour time format

                // Show the TimePickerDialog
                timePickerDialog.show();
            }
        }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }
    private void updateSelectedDateTimeTextView() {
        // Update the TextView with the chosen date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String formattedDateTime = dateFormat.format(selectedDateTime.getTime());
        selectedDateTimeTextView.setText("Chosen: " + formattedDateTime);
    }
}