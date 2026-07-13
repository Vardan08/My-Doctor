package com.example.mydoctor.workers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.mydoctor.helpers.NotificationHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VaccinationReminderWorker extends Worker {

    private static final long TIMEOUT_SECONDS = 60;

    public VaccinationReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        String userId = getInputData().getString("userId");
        if (TextUtils.isEmpty(userId)) {
            Log.w("VaccinationReminderWorker", "No userId supplied, nothing to check");
            return Result.failure();
        }

        try {
            checkVaccinations(userId);
            return Result.success();
        } catch (Exception e) {
            // Network hiccups are worth another attempt; WorkManager will back off and retry.
            Log.e("VaccinationReminderWorker", "Vaccination check failed", e);
            return Result.retry();
        }
    }

    /**
     * doWork() runs on a background thread and the worker is torn down as soon as it returns, so
     * every Firestore call has to be awaited here rather than left to an async listener.
     */
    private void checkVaccinations(String parentId) throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        QuerySnapshot children = Tasks.await(
                db.collection("users").document(parentId).collection("children").get(),
                TIMEOUT_SECONDS, TimeUnit.SECONDS);

        for (QueryDocumentSnapshot childDoc : children) {
            checkChildVaccinations(db, parentId, childDoc);
        }
    }

    private void checkChildVaccinations(FirebaseFirestore db, String userId, QueryDocumentSnapshot childDoc)
            throws Exception {
        String dob = childDoc.getString("dob");
        String childName = childDoc.getString("fullName");
        if (dob == null) return;

        QuerySnapshot vaccines = Tasks.await(
                db.collection("users").document(userId)
                        .collection("children").document(childDoc.getId())
                        .collection("vaccines").get(),
                TIMEOUT_SECONDS, TimeUnit.SECONDS);

        for (QueryDocumentSnapshot vaccineDoc : vaccines) {
            String vaccineName = vaccineDoc.getString("vaccine");
            Long ageInMonths = vaccineDoc.getLong("ageInMonths");
            Boolean status = vaccineDoc.getBoolean("status");

            // A vaccine document missing either field can't be evaluated; skip rather than crash.
            if (vaccineName == null || ageInMonths == null) continue;

            boolean alreadyReceived = Boolean.TRUE.equals(status);
            if (!alreadyReceived && isVaccinationDue(ageInMonths, dob)) {
                sendNotification(vaccineName, childName);
            }
        }
    }

    public boolean isVaccinationDue(long ageInMonths, String childDob) {
        try {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf =
                    new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            sdf.setLenient(false);
            Date dob = sdf.parse(childDob);
            if (dob == null) return false;

            // A child born in the future has nothing due yet.
            if (dob.after(new Date())) return false;

            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            Calendar today = Calendar.getInstance();

            int yearsDifference = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
            int monthsDifference = today.get(Calendar.MONTH) - dobCalendar.get(Calendar.MONTH);
            int ageInMonthsCurrent = yearsDifference * 12 + monthsDifference;

            // Not a full month has elapsed until the day-of-month is reached.
            if (today.get(Calendar.DAY_OF_MONTH) < dobCalendar.get(Calendar.DAY_OF_MONTH)) {
                ageInMonthsCurrent--;
            }

            return ageInMonthsCurrent >= ageInMonths;
        } catch (ParseException e) {
            Log.w("VaccinationReminderWorker", "Unparseable date of birth: " + childDob, e);
            return false;
        }
    }

    private void sendNotification(String vaccineName, String childName) {
        String title = "Vaccination Reminder";
        String message = "It's time for " + childName + " to take " + vaccineName + " vaccination.";
        NotificationHelper.sendNotification(getApplicationContext(), title, message);
    }
}
