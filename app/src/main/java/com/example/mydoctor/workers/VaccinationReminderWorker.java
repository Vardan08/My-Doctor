package com.example.mydoctor.workers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.WorkManager;

import com.example.mydoctor.helpers.NotificationHelper;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class VaccinationReminderWorker extends Worker {
    private FirebaseUser currentUser;

    public VaccinationReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    public void setCurrentUser(FirebaseUser currentUser){
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("VaccinationReminderWorker", "Work is being executed");
        String userId = getInputData().getString("userId");
        checkVaccinations(userId);

        // Reschedule the worker
//        scheduleNextWork();

        return Result.success();
    }

    private void checkVaccinations(String parentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(parentId).collection("children").get()
                .addOnSuccessListener(childrenSnapshots -> {
                    for (QueryDocumentSnapshot childDoc : childrenSnapshots) {
                        checkChildVaccinations(db, parentId, childDoc);
                    }
                });

    }

    private void checkChildVaccinations(FirebaseFirestore db, String userId, QueryDocumentSnapshot childDoc) {
        String childId = childDoc.getId();
        db.collection("users").document(userId).collection("children").document(childId).collection("vaccines").get()
                .addOnSuccessListener(vaccineSnapshots -> {
                    for (QueryDocumentSnapshot vaccineDoc : vaccineSnapshots) {
                        String vaccineName = vaccineDoc.getString("vaccine");
                        long ageInMonths = vaccineDoc.getLong("ageInMonths");
                        boolean status = vaccineDoc.getBoolean("status");

                        if (!status && isVaccinationDue(ageInMonths, childDoc.getString("dob"))) {
                            sendNotification(userId, childId, vaccineName, childDoc.getString("fullName"));
                        }
                    }
                });
    }
    public boolean isVaccinationDue(long ageInMonths, String childDob) {
        try {
            // Parse the child's date of birth
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date dob = sdf.parse(childDob);

            // Check if the child was born in the future
            Date todayDate = new Date();
            assert dob != null;
            if (dob.after(todayDate)) {
                System.out.println("The child's date of birth is in the future.");
                return false;
            }

            // Calculate the child's age in months
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            Calendar today = Calendar.getInstance();

            int yearsDifference = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            int monthsDifference = today.get(Calendar.MONTH) - dobCalendar.get(Calendar.MONTH);
            int ageInMonthsCurrent = yearsDifference * 12 + monthsDifference;

            // Check if the vaccination is due
            return ageInMonthsCurrent >= ageInMonths;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void sendNotification(String userId, String childId, String vaccineName, String childName) {
        String title = "Vaccination Reminder";
        String message = "It's time for "+ childName + " to take " + vaccineName + " vaccination.";
        NotificationHelper.sendNotification(getApplicationContext(), title, message);
    }
}
