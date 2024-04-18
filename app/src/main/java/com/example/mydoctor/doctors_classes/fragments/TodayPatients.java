package com.example.mydoctor.doctors_classes.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.mydoctor.R;
import com.example.mydoctor.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TodayPatients extends Fragment {

    private ArrayList<String> spinnerArrList;
    private ArrayAdapter<String> spinnerArrAdapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    private LinearLayout container;
    private User user;
    private List<DocumentSnapshot> patientSnapshots = new ArrayList<>();
    private String patientId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true); // Retain the fragment instance across configuration changes
        if (getArguments() != null) {
            user = getArguments().getParcelable("USER");
        }

        if (spinnerArrList == null) {
            spinnerArrList = new ArrayList<>();
//            spinnerArrList.add("Today Patients");
            spinnerArrList.add("All Patients");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_patients, container, false);
        Spinner spinner = view.findViewById(R.id.spinner2);
        SearchView searchView = view.findViewById(R.id.search_view);  // Add this line

        spinnerArrAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, spinnerArrList);
        spinnerArrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrAdapter);
        this.container = view.findViewById(R.id.container);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().equals("All Patients")) {
                    allPatients();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup the SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });

        return view;
    }
    private void allPatients() {
        if (currentUser != null) {
            this.container.removeAllViews();  // Clear existing views to prevent duplication
            db.collection("requests").whereEqualTo("doctorId", currentUser.getUid())
                    .whereEqualTo("status","added").get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            patientSnapshots.clear(); // Clear existing patient snapshots
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String childId = document.getString("childId");
                                fetchUserAndAddChildCard(childId);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void fetchUserAndAddChildCard(String childId) {
        db.collection("users").whereEqualTo("roll", "Patient").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        patientId = documentSnapshot.getId();
                        db.collection("users").document(patientId).collection("children").document(childId)
                                .get().addOnSuccessListener(documentSnapshot2 -> {
                                    if (documentSnapshot2.exists()) {
                                        patientSnapshots.add(documentSnapshot2);
                                        addChildCard(documentSnapshot2,patientId);
                                    }
                                });
                    }
                });
    }

    private void addChildCard(DocumentSnapshot child, String patientId) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View cardView = inflater.inflate(R.layout.card_view_layout, this.container, false);
        Map<String, Object> user = child.getData();
        assert user != null;
        ((TextView) cardView.findViewById(R.id.textViewFullName)).setText((String) user.get("fullName"));
        ((TextView) cardView.findViewById(R.id.textViewLocation)).setText((String) user.get("location"));
        ((TextView) cardView.findViewById(R.id.textViewDOB)).setText((String) user.get("dob"));
        ((TextView) cardView.findViewById(R.id.textViewDoctor)).setText((String) user.get("birthCertDetails"));
        if (user.containsKey("imageUri")) {
            Glide.with(this).load((String) user.get("imageUri")).into((ImageView) cardView.findViewById(R.id.imageViewChild));
        }
        this.container.addView(cardView);
        cardView.setOnClickListener(v -> {
            PatientData anotherFragment = new PatientData();
            anotherFragment.setPatientChildData(user, patientId);
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });
    }


    private void filter(String text) {
        container.removeAllViews(); // Clear the existing views/cards
        for (DocumentSnapshot patientSnapshot : patientSnapshots) { // Iterate through the snapshot list
            String fullName = patientSnapshot.getString("fullName");
            if (fullName != null && fullName.toLowerCase().contains(text.toLowerCase())) {
                addChildCard(patientSnapshot,patientId); // Add the card if it matches the filter
            }
        }
    }
}

//    private void openPatientQuestionnaire() {
//        DoctorQuestionnaire anotherFragment = new DoctorQuestionnaire();
//
//        androidx.fragment.app.FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
//
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//        fragmentTransaction.replace(R.id.content_of_three_buttons, anotherFragment);
//
//        fragmentTransaction.addToBackStack(null);
//
//        fragmentTransaction.commit();
//    }