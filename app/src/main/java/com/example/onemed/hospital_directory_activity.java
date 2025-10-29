package com.example.onemed;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class hospital_directory_activity extends AppCompatActivity implements DoctorAdapter.DocClickListener {

    private RecyclerView recyclerResults;
    private ViewGroup noDoctorContainer;
    private AutoCompleteTextView specialtyDropdown, doctorDropdown;
    private DoctorAdapter doctorAdapter;
    private final List<doctor> allDoctors = new ArrayList<>();

    private Context context;
    private TextInputLayout specialtyLayout,doctorLayout;

    private FirebaseFirestore db;

    private String hospitalNameFromIntent;

    private doctor selectedDoctor = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hospital_detail_activity);

        initViews();
        setupRecyclerView();
        setupClickListeners();

        // Get hospital name from intent
        hospitalNameFromIntent = getIntent().getStringExtra("hospitalName");
        if (hospitalNameFromIntent == null || hospitalNameFromIntent.isEmpty()) {
            Toast.makeText(this, "No hospital selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fetchHospitalDetails(hospitalNameFromIntent);

        // Get color from your resources
        int hintColor = ContextCompat.getColor(this, R.color.hint_color); // Replace with actual color
        ColorStateList colorStateList = ColorStateList.valueOf(hintColor);

        //set color
        specialtyLayout.setDefaultHintTextColor(colorStateList);
        doctorLayout.setDefaultHintTextColor(colorStateList);


        // Set hospital name to TextView
        TextView hospitalNameText = findViewById(R.id.Hospital_name);
        hospitalNameText.setText(hospitalNameFromIntent);

        fetchDoctorsFromFirestore(hospitalNameFromIntent);

        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;

        TextView hospitalNumberText = findViewById(R.id.hospital_number);

        if (nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            hospitalNameText.setTextColor(getResources().getColor(android.R.color.white));
            hospitalNumberText.setTextColor(getResources().getColor(android.R.color.white));
        }

    }


    private void initViews() {
        specialtyDropdown = findViewById(R.id.specialtyDropdown);
        doctorDropdown = findViewById(R.id.doctorDropdown);
        recyclerResults = findViewById(R.id.doctor_directory_recycler);
        noDoctorContainer = findViewById(R.id.noDoctorContainer);
        db = FirebaseFirestore.getInstance();
        specialtyLayout = findViewById(R.id.specialtyInputLayout);
        doctorLayout = findViewById(R.id.doctorInputLayout);
    }

    private void setupRecyclerView() {
        doctorAdapter = new DoctorAdapter(this, allDoctors, this, R.layout.item_doctor_2);
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerResults.setAdapter(doctorAdapter);
    }

    private void fetchHospitalDetails(String hospitalName) {
        db.collection("Hospital")
                .whereEqualTo("hospitalname", hospitalName)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot docSnap = querySnapshot.getDocuments().get(0);

                        // 🔸 Read values from Firestore
                        String phone = docSnap.getString("phoneNumber");
                        String imageResName = docSnap.getString("hospital_Img");
                        Double rating = docSnap.getDouble("rating");
                        GeoPoint location=docSnap.getGeoPoint("location");

                        // view in map binding
                        findViewById(R.id.view_in_map).setOnClickListener(v -> {
                            if (location != null) {
                                String uri = "geo:" + location.getLatitude() + "," + location.getLongitude() +
                                        "?q=" + Uri.encode(hospitalName);
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                                mapIntent.setPackage("com.google.android.apps.maps"); // Optional: force Google Maps
                                startActivity(mapIntent);
                            } else {
                                Toast.makeText(context, "Location not available", Toast.LENGTH_SHORT).show();
                            }

                        });

                        // call button binding
                        findViewById(R.id.call_hospital).setOnClickListener(v -> {

                            if (!phone.isEmpty() && !phone.equals("N/A")) {
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + phone));
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
                            }
                        });


                        ((TextView) findViewById(R.id.Hospital_name)).setText(hospitalName);
                        ((TextView) findViewById(R.id.hospital_number)).setText(phone != null ? phone : "N/A");

                        // Image binding
                        ImageView hospitalImage = findViewById(R.id.hospitalImage);
                        if (imageResName != null && !imageResName.isEmpty()) {
                            int resId = getResources().getIdentifier(imageResName, "drawable", getPackageName());
                            if (resId != 0) {
                                hospitalImage.setImageResource(resId);
                            } else {
                                hospitalImage.setImageResource(R.drawable.thomson); // fallback
                            }
                        }

                        // Rating binding
                        RatingBar ratingBar = findViewById(R.id.hospital_rating);
                        if (rating != null) {
                            ratingBar.setRating(rating.floatValue());
                        } else {
                            ratingBar.setRating(0f);
                        }

                    } else {
                        Toast.makeText(this, "Hospital not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to fetch hospital", e);
                    Toast.makeText(this, "Error fetching hospital data", Toast.LENGTH_SHORT).show();
                });
    }
    //end of hospital logic

    //doctor fetching
    private void fetchDoctorsFromFirestore(String hospitalName) {
        db.collection("doctors")
                .whereArrayContains("hospitals", hospitalName)
                .orderBy("docName", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allDoctors.clear();
                    List<String> specialties = new ArrayList<>();
                    List<String> doctorNames = new ArrayList<>();
                    for (DocumentSnapshot docSnap : querySnapshot) {
                        doctor doc = docSnap.toObject(doctor.class);
                        if (doc != null) {
                            allDoctors.add(doc);
                            if (!specialties.contains(doc.getSpecialty())) {
                                specialties.add(doc.getSpecialty());
                            }

                            if (!doctorNames.contains(doc.getDocName())) {
                                doctorNames.add(doc.getDocName());
                            }
                        }
                    }

                    if (allDoctors.isEmpty()) {
                        showNoDoctorFound();
                    } else {
                        showDoctorList();
                    }

                    doctorAdapter.updateList(new ArrayList<>(allDoctors));
                    setupDropdowns(specialties, doctorNames);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching doctors", e);
                    Toast.makeText(this, "Failed to load doctors.", Toast.LENGTH_SHORT).show();
                    showNoDoctorFound();
                });

    }

    // toggling visibility between no item found containers and result found
    private void showNoDoctorFound() {
        recyclerResults.setVisibility(View.GONE);
        noDoctorContainer.setVisibility(View.VISIBLE);

        if (noDoctorContainer.getChildCount() == 0) {
            View noDoctorView = LayoutInflater.from(this).inflate(R.layout.no_result_found, noDoctorContainer, false);
            noDoctorContainer.addView(noDoctorView);
        }
    }

    private void showDoctorList() {
        recyclerResults.setVisibility(View.VISIBLE);
        noDoctorContainer.setVisibility(View.GONE);
        noDoctorContainer.removeAllViews();
    }

    //doc click function
    @Override
    public void onDoctorSelected(doctor doc) {
        selectedDoctor = doc;
    }


    //listeners for button
    private void setupClickListeners() {
        findViewById(R.id.doctor_section_toggle).setOnClickListener(v -> {
            View doctorSection = findViewById(R.id.doctorSection);
            MaterialButton toggleBtn = (MaterialButton) v;
            if (doctorSection.getVisibility() == View.GONE) {
                doctorSection.setVisibility(View.VISIBLE);
                toggleBtn.setText("Hide Doctors");
            } else {
                doctorSection.setVisibility(View.GONE);
                toggleBtn.setText("Show Doctors");
            }
        });




        // bottom nav buttons
        findViewById(R.id.bookAppointment).setOnClickListener(v -> {
            Toast.makeText(this, "Book Appointment", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(hospital_directory_activity.this, BookAppointment_activity.class);
            startActivity(intent);
        });

        findViewById(R.id.homeButton).setOnClickListener(v -> {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(hospital_directory_activity.this, main_activity.class);
            startActivity(intent);
        });

        findViewById(R.id.history_btn).setOnClickListener(v -> {
            Toast.makeText(this, "History", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(hospital_directory_activity.this, history_activity.class);
            startActivity(intent);
        });

        // back button
        findViewById(R.id.back_btn).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }





    private void filterDoctorList() {
        String selectedSpecialty = specialtyDropdown.getText().toString().trim();
        String selectedDoctorName = doctorDropdown.getText().toString().trim();

        List<doctor> filteredDoctors = new ArrayList<>();

        for (doctor doc : allDoctors) {
            boolean matchSpecialty = selectedSpecialty.isEmpty() || doc.getSpecialty().equalsIgnoreCase(selectedSpecialty);
            boolean matchDoctor = selectedDoctorName.isEmpty() || doc.getDocName().equalsIgnoreCase(selectedDoctorName);

            if (matchSpecialty && matchDoctor) {
                filteredDoctors.add(doc);
            }
        }

        doctorAdapter.updateList(filteredDoctors);

        if (!filteredDoctors.contains(selectedDoctor)) {
            selectedDoctor = null;
            
        }

        if (filteredDoctors.isEmpty()) {
            showNoDoctorFound();
        } else {
            showDoctorList();
        }
    }
    private void setupDropdowns(List<String> specialties, List<String> doctorNames) {
        ArrayAdapter<String> specialtyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, specialties);
        specialtyDropdown.setAdapter(specialtyAdapter);

        ArrayAdapter<String> doctorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, doctorNames);
        doctorDropdown.setAdapter(doctorAdapter);

        specialtyDropdown.setOnItemClickListener((parent, view, position, id) -> filterDoctorList());
        doctorDropdown.setOnItemClickListener((parent, view, position, id) -> filterDoctorList());

        specialtyDropdown.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctorList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        doctorDropdown.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctorList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDoctorBookClicked(doctor selectedDoctor) {
        Intent intent = new Intent(this, BookAppointment_activity.class);
        intent.putExtra("SelectedDocName", selectedDoctor.getDocName());
        intent.putExtra("isRescheduleFlow", true);
        startActivity(intent);
    }



}
