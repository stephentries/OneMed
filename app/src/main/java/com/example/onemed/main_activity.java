package com.example.onemed;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;




import java.util.ArrayList;
import java.util.List;

public class main_activity extends AppCompatActivity {

    private static final String TAG = "main_activity";

    private ImageView settings, profilePic;
    private ImageButton medicalRecordsBtn, searchHospitalBtn, searchDoctorBtn;
    private ImageButton bookAppointmentBtn, homeButton, historyBtn;
    private RecyclerView prescriptionRecyclerView, appointmentRecyclerView,hospitalRecyclerView;
    private LinearLayout dotsContainer, dotsContainer2;
    private FrameLayout noAppointmentContainer, noPrescriptionContainer;

    private AppointmentAdapter appointmentAdapter;
    private PrescriptionAdapter prescriptionAdapter;
    private HospitalAdapter hospitalAdapter;

    private List<appointment> appointmentList = new ArrayList<>();
    private List<prescription> prescriptionList = new ArrayList<>();
    private List<Hospital> hospitalList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);


        initViews();
        setupListeners();
        setupRecyclerViews();
        loadDynamicAppointments();
        loadPrescriptions();
    }

    private void initViews() {
        profilePic = findViewById(R.id.profile_pic);
        settings = findViewById(R.id.settings);

        //medicalRecordsBtn = findViewById(R.id.medical_records);
        //searchHospitalBtn = findViewById(R.id.search_hospital);
        //searchDoctorBtn = findViewById(R.id.search_doctor);

        bookAppointmentBtn = findViewById(R.id.bookAppointment);
        homeButton = findViewById(R.id.homeButton);
        historyBtn = findViewById(R.id.history_btn);

        appointmentRecyclerView = findViewById(R.id.AppointmentRecyclerView);
        dotsContainer = findViewById(R.id.dotsContainer);
        noAppointmentContainer = findViewById(R.id.noAppointmentContainer);


        prescriptionRecyclerView = findViewById(R.id.prescriptionRecyclerView);
        dotsContainer2 = findViewById(R.id.dotsContainer2);
        noPrescriptionContainer = findViewById(R.id.noPrescriptionContainer);
        hospitalRecyclerView = findViewById(R.id.hospitalRecyclerView);
    }

    private void setupListeners() {
        settings.setOnClickListener(v -> {showToast("Settings");
            Intent intent= new Intent(main_activity.this,settings_activity.class);
            startActivity(intent);
        });
        //medicalRecordsBtn.setOnClickListener(v -> {
            //showToast("Medical Records");
            //startActivity(new Intent(main_activity.this, medical_records_activity.class));
        //});

        bookAppointmentBtn.setOnClickListener(v -> {
            startActivity(new Intent(main_activity.this, BookAppointment_activity.class));
            showToast("Book appointment");
        });

        homeButton.setOnClickListener(v -> showToast("Home"));

        historyBtn.setOnClickListener(v -> {
            startActivity(new Intent(main_activity.this, history_activity.class));
            showToast("Appointment and prescription History");
        });
    }

    private void setupRecyclerViews() {
        // Appointments RecyclerView setup
        LinearLayoutManager appointmentLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        appointmentRecyclerView.setLayoutManager(appointmentLayoutManager);
        appointmentAdapter = new AppointmentAdapter(this, appointmentList);
        appointmentRecyclerView.setAdapter(appointmentAdapter);
        appointmentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int position = appointmentLayoutManager.findFirstVisibleItemPosition();
                addDots(dotsContainer, appointmentList.size(), position);
            }
        });

        // Prescriptions RecyclerView setup
        LinearLayoutManager prescriptionLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        prescriptionRecyclerView.setLayoutManager(prescriptionLayoutManager);
        prescriptionAdapter = new PrescriptionAdapter(this, prescriptionList);
        prescriptionRecyclerView.setAdapter(prescriptionAdapter);
        prescriptionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                int position = prescriptionLayoutManager.findFirstVisibleItemPosition();
                addDots(dotsContainer2, prescriptionList.size(), position);
            }
        });

        // hospital list recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        hospitalRecyclerView.setLayoutManager(layoutManager);

        hospitalAdapter = new HospitalAdapter(this, hospitalList);
        hospitalRecyclerView.setAdapter(hospitalAdapter);

        hospitalRecyclerView.setClipToPadding(false);
        hospitalRecyclerView.setPadding(16, 0, 16, 0);
        loadHospitalData();
    }

    private void loadDynamicAppointments() {
        appointmentList.clear();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User not logged in, can't load appointments");
            showNoAppointmentsView();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("appointments")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore error loading appointments: " + error.getMessage());
                        showNoAppointmentsView();
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        showNoAppointmentsView();
                        return;
                    }

                    appointmentList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String doctorName = doc.getString("doctorName");
                        String specialty = doc.getString("specialty");
                        String date = doc.getString("date");
                        String time = doc.getString("time");
                        String hospital = doc.getString("hospital");
                        Boolean isTaken = doc.getBoolean("istaken");
                        String userEmail = doc.getString("userEmail");
                        String userId = doc.getString("userId");
                        String id = doc.getId();
                        Float rating = doc.getDouble("rating") != null ? doc.getDouble("rating").floatValue() : 0f;
                        String review = doc.getString("review");

                        boolean valid = doctorName != null && !doctorName.trim().isEmpty()
                                && specialty != null && !specialty.trim().isEmpty()
                                && date != null && !date.trim().isEmpty()
                                && time != null && !time.trim().isEmpty()
                                && hospital != null && !hospital.trim().isEmpty()
                                && (isTaken != null);

                        if (valid && !isTaken) {
                            appointmentList.add(new appointment(
                                    doctorName,
                                    specialty,
                                    date,
                                    time,
                                    hospital,
                                    isTaken,
                                    userEmail,
                                    userId,
                                    id
                            ));
                        } else {
                            Log.w(TAG, "Skipping invalid or taken appointment doc: " + doc.getId());
                        }
                    }

                    if (appointmentList.isEmpty()) {
                        showNoAppointmentsView();
                    } else {
                        noAppointmentContainer.setVisibility(View.GONE);
                        appointmentRecyclerView.setVisibility(View.VISIBLE);
                        dotsContainer.setVisibility(View.VISIBLE);
                        appointmentAdapter.notifyDataSetChanged();
                        addDots(dotsContainer, appointmentList.size(), 0);
                    }
                });
    }


    private void showNoAppointmentsView() {
        appointmentRecyclerView.setVisibility(View.GONE);
        dotsContainer.setVisibility(View.GONE);
        noAppointmentContainer.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.no_appointment, noAppointmentContainer, false);
        MaterialButton bookBtn = view.findViewById(R.id.book_new_appt);
        bookBtn.setOnClickListener(v -> startActivity(new Intent(main_activity.this, BookAppointment_activity.class)));
        noAppointmentContainer.addView(view);
        noAppointmentContainer.setVisibility(View.VISIBLE);
    }

    private void loadPrescriptions() {
        prescriptionList.clear();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Log.e(TAG, "User not logged in, can't load appointments");
            showNoAppointmentsView();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).collection("prescription")
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore error loading prescriptions: " + error.getMessage());
                        showNoPrescriptionsView();
                        return;
                    }

                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.isEmpty()) {
                        showNoPrescriptionsView();
                        return;
                    }

                    prescriptionList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String medName = doc.getString("medicineName");
                        String dosage = doc.getString("dosage");
                        String instruction = doc.getString("instruction");
                        Boolean isFinished = doc.getBoolean("isFinished");

                        Long currentDoseLong = doc.getLong("currentDoseCount");
                        if (currentDoseLong == null) currentDoseLong = doc.getLong("CurrentDoseCount");
                        Long maxDoseLong = doc.getLong("maxDoseCount");

                        int currentDose = currentDoseLong != null ? currentDoseLong.intValue() : 0;
                        int maxDose = maxDoseLong != null ? maxDoseLong.intValue() : 0;

                        if (medName != null && dosage != null && instruction != null &&!isFinished) {
                            prescriptionList.add(new prescription(doc.getId(), medName, dosage, instruction, maxDose, currentDose, isFinished != null && isFinished));
                        }
                    }

                    if (prescriptionList.isEmpty()) {
                        showNoPrescriptionsView();
                    } else {
                        noPrescriptionContainer.setVisibility(View.GONE);
                        prescriptionRecyclerView.setVisibility(View.VISIBLE);
                        dotsContainer2.setVisibility(View.VISIBLE);
                        prescriptionAdapter.notifyDataSetChanged();
                        addDots(dotsContainer2, prescriptionList.size(), 0);
                    }
                });
    }

    private void showNoPrescriptionsView() {
        prescriptionRecyclerView.setVisibility(View.GONE);
        dotsContainer2.setVisibility(View.GONE);
        noPrescriptionContainer.removeAllViews();
        View view = LayoutInflater.from(this).inflate(R.layout.no_prescription, noPrescriptionContainer, false);
        noPrescriptionContainer.addView(view);
        noPrescriptionContainer.setVisibility(View.VISIBLE);
    }

    private void addDots(LinearLayout container, int count, int selectedPosition) {
        container.removeAllViews();
        int size = (int) (8 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < count; i++) {
            ImageView dot = new ImageView(this);
            dot.setLayoutParams(params);
            dot.setImageResource(i == selectedPosition ? R.drawable.dot_active : R.drawable.dot_inactive);
            container.addView(dot);
        }
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void loadHospitalData() {
        FirebaseFirestore.getInstance().collection("Hospital")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    hospitalList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Hospital hospital = doc.toObject(Hospital.class);
                        hospitalList.add(hospital);
                    }
                    hospitalAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load hospitals", Toast.LENGTH_SHORT).show()
                );
    }
}
