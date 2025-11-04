package com.example.onemed;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BookAppointment_activity extends AppCompatActivity implements DoctorAdapter.DocClickListener {

    private AutoCompleteTextView specialtyDropdown, doctorDropdown, hospitalDropdown;
    private TextInputEditText dateInput;
    private Spinner timeSlotSpinner;
    private Button bookButton;
    private ImageView backBtn;
    private ViewGroup noDoctorContainer;
    private ImageButton homeButton, bookAppointment, history_btn;

    private TextInputLayout specialtyLayout,doctorLayout,hospitalLayout,dateinputLayout;
    private List<doctor> allDoctors = new ArrayList<>();
    private DoctorAdapter doctorAdapter;
    private RecyclerView recyclerResults;

    private boolean isDateSelected = false;
    private boolean isTimeSlotSelected = false;
    private boolean isDoctorCardselected = false;

    private doctor selectedDoctor = null;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_appointment);


        initViews();


        // Get color from your resources
        int hintColor = ContextCompat.getColor(this, R.color.hint_color); // Replace with actual color
        ColorStateList colorStateList = ColorStateList.valueOf(hintColor);

// Set it programmatically
        specialtyLayout.setDefaultHintTextColor(colorStateList);
        doctorLayout.setDefaultHintTextColor(colorStateList);
        hospitalLayout.setDefaultHintTextColor(colorStateList);
        dateinputLayout.setDefaultHintTextColor(colorStateList);

        setupDropdowns();
        loadRecyclerResults();
        setupBookButton();
        setuplisteners();
        setupDatePicker();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        Intent intent = getIntent();
        boolean isRescheduleFlow = intent.getBooleanExtra("isRescheduleFlow", false);
        String doctorToPreselect = intent.getStringExtra("SelectedDocName");
        Log.d("BookAppointment", "RescheduleFlow: " + isRescheduleFlow + ", SelectedDocName: " + doctorToPreselect);
        fetchDoctorsFromFirestore(doctorToPreselect);

        if(isRescheduleFlow) {
            bookButton.setText("Reschedule");
        }

    }

    private void initViews() {
        specialtyDropdown = findViewById(R.id.specialtyDropdown);
        doctorDropdown = findViewById(R.id.doctorDropdown);
        hospitalDropdown = findViewById(R.id.hospitalDropdown);
        dateInput = findViewById(R.id.dateInput);
        timeSlotSpinner = findViewById(R.id.timeSlotSpinner);
        bookButton = findViewById(R.id.bookButton);
        homeButton = findViewById(R.id.homeButton);
        bookAppointment = findViewById(R.id.bookAppointment);
        history_btn = findViewById(R.id.history_btn);
        noDoctorContainer = findViewById(R.id.noDoctorContainer);
        backBtn = findViewById(R.id.back_btn);

        specialtyLayout = findViewById(R.id.specialtyInputLayout);
        doctorLayout = findViewById(R.id.doctorInputLayout);
        hospitalLayout = findViewById(R.id.hospitalInputLayout);
        dateinputLayout=findViewById(R.id.dateInputLayout);
    }


    private void setupDropdowns() {
        String[] specialties = {"Cardiology", "Dermatology", "General Surgery", "Paediatrician"};
        String[] doctors = {"Dr. Al Fazir Omar", "Dr. Bong Jan Ling", "Dr. Balraj Singh", "Dr. Anita Kaur Ahluwalia"};

        setupAutoCompleteDropdown(specialtyDropdown, specialties, value -> filterDoctorList());
        setupAutoCompleteDropdown(doctorDropdown, doctors, value -> filterDoctorList());

        String[] hospitals = {
                "Thomson Hospital Kota Damansara",
                "Gleneagles Hospital Kuala Lumpur",
                "Pantai Hospital Kuala Lumpur",
                "Sunway Medical Centre"
        };
        setupAutoCompleteDropdown(hospitalDropdown, hospitals, value -> filterDoctorList());
    }

    private void setupAutoCompleteDropdown(AutoCompleteTextView dropdown, String[] items, OnItemSelected callback) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemClickListener((parent, view, position, id) -> callback.onSelected(items[position]));

        dropdown.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctorList();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupDatePicker() {
        dateInput.setOnClickListener(v -> {
            if (selectedDoctor == null) {
                Toast.makeText(this, "Please select a doctor first", Toast.LENGTH_SHORT).show();
                return;
            }

            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, null, year, month, day);
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

            dialog.setOnShowListener(dialogInterface -> {
                Button okButton = dialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
                okButton.setOnClickListener(view -> {
                    DatePicker dp = dialog.getDatePicker();
                    int y = dp.getYear();
                    int m = dp.getMonth();
                    int d = dp.getDayOfMonth();

                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, y);
                    selected.set(Calendar.MONTH, m);
                    selected.set(Calendar.DAY_OF_MONTH, d);
                    selected.set(Calendar.HOUR_OF_DAY, 0);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);

                    int dayOfWeek = selected.get(Calendar.DAY_OF_WEEK);
                    boolean allowed = false;
                    for (int available : selectedDoctor.getAvailableDays()) {
                        if (available == dayOfWeek) {
                            allowed = true;
                            break;
                        }
                    }

                    if (allowed) {
                        dateInput.setText(d + "/" + (m + 1) + "/" + y);
                        isDateSelected = true;
                        validateForm();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this,
                                "Doctor not available on that day.\nAvailable: " +
                                        getAvailableDaysString(selectedDoctor.getAvailableDays()),
                                Toast.LENGTH_LONG).show();
                    }
                });
            });

            dialog.show();
        });
    }

    private String getAvailableDaysString(List<Integer> days) {
        StringBuilder sb = new StringBuilder();
        for (int d : days) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, d);
            sb.append(android.text.format.DateFormat.format("EEEE", cal)).append(", ");
        }
        return sb.length() > 2 ? sb.substring(0, sb.length() - 2) : sb.toString();
    }

    private void updateTimeSlotSpinner() {
        if (selectedDoctor != null) {
            List<String> timeSlots = selectedDoctor.getAvailableTimeSlots();
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_dropdown_item, timeSlots);
            timeSlotSpinner.setAdapter(spinnerAdapter);
            timeSlotSpinner.setSelection(0);
            isTimeSlotSelected = false;

            timeSlotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    isTimeSlotSelected = position != -1;
                    validateForm();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {
                    isTimeSlotSelected = false;
                    validateForm();
                }
            });
        }
    }

    private void updateHospitalDropdown() {
        if (selectedDoctor != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, selectedDoctor.getHospitals());
            hospitalDropdown.setAdapter(adapter);
            hospitalDropdown.setText("");
        }
    }

    private void setupBookButton() {
        Bundle extras= getIntent().getExtras();

        bookButton.setOnClickListener(v -> {
            if (selectedDoctor == null) {
                Toast.makeText(this, "Please select a doctor", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            if(extras!=null){
                Boolean isRescheduleFlow=extras.getBoolean("isRescheduleFlow");
                String SelectedDocName=extras.getString("SelectedDocName");
                String appointmentId = extras.getString("appointmentId");

                if (isRescheduleFlow ) {
                    String selectedDate = dateInput.getText().toString().trim();
                    String selectedTimeSlot = timeSlotSpinner.getSelectedItem().toString().trim();
                    updateAppointmentFromReschedule(appointmentId,selectedDate,selectedTimeSlot);
                    return;

                }

            }

            String selectedDate = dateInput.getText().toString().trim();
            String selectedTimeSlot = timeSlotSpinner.getSelectedItem().toString().trim();
            String selectedHospital = hospitalDropdown.getText().toString().trim();

            String confirmationMsg = "Appointment booked with " + selectedDoctor.getDocName() +
                    " on " + selectedDate + " at " + selectedTimeSlot + " in " + selectedHospital;

            Toast.makeText(this, confirmationMsg, Toast.LENGTH_LONG).show();

            Map<String, Object> appointment = new HashMap<>();
            appointment.put("doctorName", selectedDoctor.getDocName());
            appointment.put("specialty", selectedDoctor.getSpecialty());
            appointment.put("date", selectedDate);
            appointment.put("time", selectedTimeSlot);

            // Use selectedHospital if filled, else fallback to first hospital in doctor's list
            String hospital = selectedHospital.isEmpty() ?
                    (selectedDoctor.getHospitals() != null && !selectedDoctor.getHospitals().isEmpty()
                            ? selectedDoctor.getHospitals().get(0) : "")
                    : selectedHospital;
            appointment.put("hospital", hospital);

            appointment.put("istaken", false);
            appointment.put("userId", user.getUid());
            appointment.put("userEmail", user.getEmail());

            // Save to user-specific subcollection
            db.collection("users")
                    .document(user.getUid())
                    .collection("appointments")
                    .add(appointment)
                    .addOnSuccessListener(documentReference -> {
                        String generatedId = documentReference.getId();
                        Log.d("Firestore", "Appointment saved with ID: " + generatedId);

                        // Add the ID into the appointment document
                        documentReference.update("id", generatedId)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Appointment ID updated in Firestore"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update ID in appointment", e));
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error adding appointment", e);
                    });

            finish();
        });
    }

    private void validateForm() {
        boolean enable = isDoctorCardselected && isDateSelected && isTimeSlotSelected;
        bookButton.setEnabled(enable);
    }

    interface OnItemSelected {
        void onSelected(String value);
    }

    private void setuplisteners() {
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookAppointment_activity.this, main_activity.class);
            startActivity(intent);
        });

        history_btn.setOnClickListener(v -> {
            Intent intent = new Intent(BookAppointment_activity.this, history_activity.class);
            startActivity(intent);
        });

        backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void filterDoctorList() {
        String selectedSpecialty = specialtyDropdown.getText().toString().trim();
        String selectedDoctorName = doctorDropdown.getText().toString().trim();
        String selectedHospital = hospitalDropdown.getText().toString().trim();

        List<doctor> filteredDoctors = new ArrayList<>();

        for (doctor doc : allDoctors) {
            boolean matchSpecialty = selectedSpecialty.isEmpty() || doc.getSpecialty().equals(selectedSpecialty);
            boolean matchDoctor = selectedDoctorName.isEmpty() || doc.getDocName().equals(selectedDoctorName);
            boolean matchHospital = selectedHospital.isEmpty() ||
                    doc.getHospitals().stream()
                            .anyMatch(h -> h.replace("\n", " ").trim().equalsIgnoreCase(selectedHospital));

            if (matchSpecialty && matchDoctor && matchHospital) {
                filteredDoctors.add(doc);
            }
        }

        doctorAdapter.updateList(filteredDoctors);

        if (!filteredDoctors.contains(selectedDoctor)) {
            selectedDoctor = null;
            isDoctorCardselected = false;
            validateForm();
        }

        if (filteredDoctors.isEmpty()) {
            recyclerResults.setVisibility(View.GONE);
            noDoctorContainer.setVisibility(View.VISIBLE);

            if (noDoctorContainer.getChildCount() == 0) {
                View noDoctorView = LayoutInflater.from(this).inflate(R.layout.no_result_found, noDoctorContainer, false);
                noDoctorContainer.addView(noDoctorView);
            }
        } else {
            recyclerResults.setVisibility(View.VISIBLE);
            noDoctorContainer.setVisibility(View.GONE);
            noDoctorContainer.removeAllViews();
        }
    }

    @Override
    public void onDoctorSelected(doctor doc) {
        selectedDoctor = doc;
        isDoctorCardselected = true;
        validateForm();
        updateHospitalDropdown();
        updateTimeSlotSpinner();
        dateInput.setText("");
        isDateSelected = false;
    }

    private void loadRecyclerResults() {
        recyclerResults = findViewById(R.id.recyclerResults);
        recyclerResults.setLayoutManager(new LinearLayoutManager(this));

        doctorAdapter = new DoctorAdapter(this, allDoctors, this,R.layout.item_doctor);
        recyclerResults.setAdapter(doctorAdapter);
    }

    private void fetchDoctorsFromFirestore(@Nullable String doctorToPreselect) {
        db.collection("doctors")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allDoctors.clear();
                    for (DocumentSnapshot docSnap : querySnapshot) {
                        doctor doc = docSnap.toObject(doctor.class);
                        if (doc != null) {
                            allDoctors.add(doc);
                        }
                    }
                    if (doctorToPreselect != null) {
                        preselectDoctorByName(doctorToPreselect);
                    }

                    if (doctorAdapter != null) {
                        doctorAdapter.updateList(new ArrayList<>(allDoctors));
                    }

                    filterDoctorList();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching doctors", e);
                    Toast.makeText(this, "Failed to load doctors.", Toast.LENGTH_SHORT).show();
                });
    }


    private void preselectDoctorByName(String name) {
        Log.d("DEBUG", "Trying to preselect doctor: " + name);
        for (doctor doc : allDoctors) {
            if (doc.getDocName().equalsIgnoreCase(name.trim())) {
                Log.d("DEBUG", "Preselecting doctor: " + doc.getDocName());
                onDoctorSelected(doc); // Reuse existing selection logic
                doctorAdapter.setSelectedDoctor(name);
                doctorDropdown.setText(doc.getDocName(), false); // Update dropdown text
                break;
            }
        }


    }
    private void scrollToDoctor(String doctorName) {
        for (int i = 0; i < allDoctors.size(); i++) {
            if (allDoctors.get(i).getDocName().equalsIgnoreCase(doctorName)) {
                recyclerResults.scrollToPosition(i);
                break;
            }
        }
    }
    @Override
    public void onDoctorBookClicked(doctor doc) {
        // implement or leave empty if not used
    }

    // update appointment if reschedule
    private void updateAppointmentFromReschedule(String appointmentID,
                                                 String newDate,
                                                 String newTime) {
        if (appointmentID == null || appointmentID.isEmpty()) {
            Toast.makeText(this, "No appointment id to reschedule.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(this, "User not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(uid)
                .collection("appointments")
                .document(appointmentID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Appointment not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String originalDate = documentSnapshot.getString("date");
                    String originalTime = documentSnapshot.getString("time");

                   // ensure reschedule date and time is not same as original
                    if (newDate.equals(originalDate) && newTime.equals(originalTime)) {
                        Toast.makeText(this, "You cannot reschedule to the same date and time.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // update date and time
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("date", newDate);
                    updates.put("time", newTime);

                    db.collection("users")
                            .document(uid)
                            .collection("appointments")
                            .document(appointmentID)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Appointment rescheduled", Toast.LENGTH_SHORT).show();

                                Intent result = new Intent();
                                result.putExtra("updatedAppointmentId", appointmentID);
                                result.putExtra("updatedDate", newDate);
                                result.putExtra("updatedTime", newTime);

                                setResult(Activity.RESULT_OK, result);
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to reschedule: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch appointment: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }



}
