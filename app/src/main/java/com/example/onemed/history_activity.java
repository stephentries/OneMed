package com.example.onemed;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class history_activity extends AppCompatActivity {

    private androidx.appcompat.widget.SearchView searchView;
    private ViewGroup noResultContainer;
    private View noResultView;
    private TabLayout tabLayout;
    private ImageView back_btn;
    private RecyclerView historyRecyclerView;
    private ImageButton bookAppointment, homeButton, history_btn;

    private FirebaseFirestore db;
    private String currentUserId;

    private AppointmentHistoryAdapter appointmentAdapter;
    private PrescriptionHistoryAdapter prescriptionAdapter;

    private List<appointment> appointmentList = new ArrayList<>();
    private List<prescription> prescriptionList = new ArrayList<>();

    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        // View references
        searchView = findViewById(R.id.search_bar);
        searchView.setIconifiedByDefault(false);
        searchView.setIconified(false);
        searchView.clearFocus();

        // Customize SearchView hint appearance
        EditText searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchEditText != null) {
            searchEditText.setHint("Search past appointment or prescription here...");
            searchEditText.setHintTextColor(getResources().getColor(android.R.color.darker_gray));
            searchEditText.setTextSize(12);
            searchEditText.setTypeface(Typeface.DEFAULT);
            searchEditText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        }

        noResultContainer = findViewById(R.id.no_result_container);
        noResultView = LayoutInflater.from(this).inflate(R.layout.no_result_found, noResultContainer, false);

        tabLayout = findViewById(R.id.tabLayout);
        back_btn = findViewById(R.id.back_btn);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        bookAppointment = findViewById(R.id.bookAppointment);
        homeButton = findViewById(R.id.homeButton);
        history_btn = findViewById(R.id.history_btn);

        // Detect if dark theme is active
        int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkTheme = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (isDarkTheme) {
            // 1. SearchView hint color in dark mode
            searchEditText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
            if (searchEditText != null) {
                int hintColor = ContextCompat.getColor(this, android.R.color.darker_gray); // or a custom light gray
                searchEditText.setHintTextColor(hintColor);
            }
            ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_mag_icon);
            if (searchIcon != null) {
                searchIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            }

            // 2. TabLayout selected tab text color in dark mode
            int tealColor = ContextCompat.getColor(this, R.color.teal_200);
            int unselectedColor = ContextCompat.getColor(this, android.R.color.darker_gray);

            tabLayout.setTabTextColors(unselectedColor, tealColor); // runtime override
        }


        // Setup Firestore
        db = FirebaseFirestore.getInstance();
        currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        // Setup adapters
        appointmentAdapter = new AppointmentHistoryAdapter(this, appointmentList);
        prescriptionAdapter = new PrescriptionHistoryAdapter(this, prescriptionList);

        // Setup RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(appointmentAdapter);
        loadAppointmentHistory();

        // Search filter logic
        searchView.setQueryHint("Search past appointment or prescription here...");
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterResults(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterResults(newText);
                return false;
            }
        });

        // Tab switching logic
        tabLayout.addTab(tabLayout.newTab().setText("Appointment History"));
        tabLayout.addTab(tabLayout.newTab().setText("Prescription History"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    historyRecyclerView.setAdapter(appointmentAdapter);
                    loadAppointmentHistory();
                } else {
                    historyRecyclerView.setAdapter(prescriptionAdapter);
                    loadPrescriptionHistory();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Navigation
        back_btn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        homeButton.setOnClickListener(v -> startActivity(new Intent(history_activity.this, main_activity.class)));
        bookAppointment.setOnClickListener(v -> startActivity(new Intent(history_activity.this, BookAppointment_activity.class)));
    }

    private void loadAppointmentHistory() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("appointments")
                .whereEqualTo("istaken", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    appointmentList.clear();
                    Log.d("APPT_DEBUG", "Total docs found: " + queryDocumentSnapshots.size());
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Log.d("APPT_DEBUG", "Doc: " + doc.getData());
                        appointment a = doc.toObject(appointment.class);
                        appointmentList.add(a);
                    }
                    updateVisibility(appointmentList.isEmpty());
                    appointmentAdapter.updateList(new ArrayList<>(appointmentList));
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading appointments", e);
                    updateVisibility(true);
                });

    }


    private void loadPrescriptionHistory() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("prescription")
                .whereEqualTo("isFinished", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    prescriptionList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        prescription p = doc.toObject(prescription.class);
                        prescriptionList.add(p);
                    }
                    updateVisibility(prescriptionList.isEmpty());
                    prescriptionAdapter.updateList(new ArrayList<>(prescriptionList));
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error loading prescriptions", e);
                    updateVisibility(true);
                });
    }

    private void updateVisibility(boolean isEmpty) {
        if (isEmpty) {
            historyRecyclerView.setVisibility(View.GONE);
            if (noResultView.getParent() == null) {
                noResultContainer.addView(noResultView);
            }
            noResultContainer.setVisibility(View.VISIBLE);
        } else {
            historyRecyclerView.setVisibility(View.VISIBLE);
            noResultContainer.removeAllViews();
            noResultContainer.setVisibility(View.GONE);
        }
    }

    private void filterResults(String query) {
        query = query.toLowerCase();

        if (tabLayout.getSelectedTabPosition() == 0) {
            List<appointment> filteredAppointments = new ArrayList<>();
            for (appointment a : appointmentList) {
                if (a.matchesQuery(query)) {
                    filteredAppointments.add(a);
                }
            }
            appointmentAdapter.updateList(filteredAppointments);
            updateVisibility(filteredAppointments.isEmpty());
        } else {
            List<prescription> filteredPrescriptions = new ArrayList<>();
            for (prescription p : prescriptionList) {
                if (p.matchesQuery(query)) {
                    filteredPrescriptions.add(p);
                }
            }
            prescriptionAdapter.updateList(filteredPrescriptions);
            updateVisibility(filteredPrescriptions.isEmpty());
        }
    }
}
